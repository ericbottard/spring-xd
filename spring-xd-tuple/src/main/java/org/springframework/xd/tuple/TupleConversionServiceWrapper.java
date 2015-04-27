/*
 * Copyright 2014 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.tuple;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.util.Assert;


/**
 * @author David Turanski
 * 
 * A decorator for {@link ConfigurableConversionService} that provides access to DateFormat and Locale, used to
 * enhance serialization performance.
 *
 */
public class TupleConversionServiceWrapper {
	private final ConfigurableConversionService configurableConversionService;

	private DateFormat dateFormat;

	private Locale locale;

	public TupleConversionServiceWrapper(ConfigurableConversionService configurableConversionService) {
		Assert.notNull(configurableConversionService, "'configurableConversionService' cannot be null");
		this.configurableConversionService = configurableConversionService;
	}

	public ConfigurableConversionService getConfigurableConversionService() {
		return configurableConversionService;
	}


	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
		configurableConversionService.addConverter(new StringToDateConverter(dateFormat));
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		configurableConversionService.addConverterFactory(new LocaleAwareStringToNumberConverterFactory(NumberFormat
				.getInstance(locale)));
	}

}
