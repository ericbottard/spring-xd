/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.dirt.rest;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author ebottard
 */
@Controller
public class RESTDocumentation {

	@Autowired
	private ApplicationContext applicationContext;

	@RequestMapping("/foo")
	public void foo(OutputStream os) {
		Set<String> controllerNames = new TreeSet<String>();
		controllerNames.addAll(Arrays.asList(applicationContext.getBeanNamesForAnnotation(Controller.class)));
		controllerNames.addAll(Arrays.asList(applicationContext.getBeanNamesForAnnotation(RestController.class)));

		final PrintStream out = new PrintStream(os);

		final Map<String, String> urlMap = new TreeMap<String, String>();
		for (String beanName : controllerNames) {
			Object bean = applicationContext.getBean(beanName);
			RequestMapping classMapping = bean.getClass().getAnnotation(RequestMapping.class);
			final String prefix = classMapping != null ? classMapping.value()[0] : "";

			ReflectionUtils.doWithMethods(bean.getClass(),
					new MethodCallback() {

						@Override
						public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
							RequestMapping rm = method.getAnnotation(RequestMapping.class);
							String suffix = rm.value().length > 0 ? rm.value()[0] : "";
							String qs = rm.params().length > 0 ? "?" + rm.params()[0] : "";
							String httpMethod = rm.method().length > 0 ? rm.method()[0].toString() : "";
							String url = prefix + suffix + qs;

							urlMap.put(url + "§" + httpMethod, String.format(" %s (%s)", method.getName(),
									method.getDeclaringClass().getSimpleName()));

							//							out.format("%6s %-70s %s (%s)%n", httpMethod, url, method.getName(),
							//									method.getDeclaringClass().getSimpleName());
						}
					},
					new MethodFilter() {

						@Override
						public boolean matches(Method method) {
							return method.getAnnotation(RequestMapping.class) != null;
						}

					});

		}

		//		out.println(urlMap.toString().replace(",", "\n"));
		out.println("<html><table>");
		for (Map.Entry<String, String> kv : urlMap.entrySet()) {
			String method = kv.getKey().replaceAll("(.*)§(.*)", "$2");
			String url = kv.getKey().replaceAll("(.*)§(.*)", "$1");
			out.format("<tr><td><input type=checkbox></td><td> %s</td> <td>%s</td> <td>%s</td>%n", method, url,
					kv.getValue());
		}

		out.println("</table></html>");

		//		out.println("*****");
		//
		//		for (String k : urlMap.keySet()) {
		//			out.format("%-80s %s%n", k.replaceAll("(.*)§(.*)", "$2 $1"), urlMap.get(k));
		//		}

	}
}
