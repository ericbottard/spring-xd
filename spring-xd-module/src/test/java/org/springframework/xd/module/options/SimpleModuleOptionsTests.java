/*
 * Copyright 2013 the original author or authors.
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

package org.springframework.xd.module.options;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Properties;

import org.junit.Test;

import org.springframework.core.env.PropertySource;


/**
 * Test for {@link SimpleModuleOptions} behavior.
 * 
 * @author Eric Bottard
 */
public class SimpleModuleOptionsTests {

	private SimpleModuleOptions moduleOptions = new SimpleModuleOptions();

	@Test
	public void testEmptyBehavior() {
		assertThat(moduleOptions.iterator(), is(notNullValue(Iterator.class)));
		assertThat(moduleOptions.iterator().hasNext(), is(false));
	}

	@Test
	public void testNonEmptyBehavior() {
		moduleOptions.add(new ModuleOption("foo", "d"));
		moduleOptions.add(new ModuleOption("bar", "d").withDefaultValue("here"));

		Iterator<ModuleOption> it = moduleOptions.iterator();

		assertThat(it.next(), hasProperty("name", equalTo("foo")));
		assertThat(it.next(), hasProperty("name", equalTo("bar")));
	}

	@Test
	public void testInterpolation() {
		moduleOptions.add(new ModuleOption("foo", "d"));
		moduleOptions.add(new ModuleOption("bar", "d").withDefaultValue("here"));
		moduleOptions.add(new ModuleOption("fizz", "d").withDefaultValue("there"));

		Properties userValues = new Properties();
		userValues.setProperty("foo", "one");
		userValues.setProperty("bar", "two");
		userValues.setProperty("bong", "gotcha");

		PropertySource<?> ps = moduleOptions.interpolate(userValues).asPropertySource();
		assertThat((String) ps.getProperty("foo"), equalTo("one"));
		assertThat((String) ps.getProperty("bar"), equalTo("two"));
		assertThat(ps.getProperty("bang"), nullValue());
		assertThat((String) ps.getProperty("fizz"), equalTo("there"));

	}
}
