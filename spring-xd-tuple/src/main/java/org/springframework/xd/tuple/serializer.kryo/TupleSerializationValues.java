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

package org.springframework.xd.tuple.serializer.kryo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.xd.tuple.DefaultTuple;
import org.springframework.xd.tuple.DefaultTupleConversionService;
import org.springframework.xd.tuple.TupleBuilder;

/**
 * A lighweight values object holding serialized Tuple state to avoid unnecessary instantiation of expensive objects.
 * @author David Turanski
 */
class TupleSerializationValues {
	public List<String> names;

	public List<Object> values;

	public DateFormat dateFormat;

	public Locale locale;

	public ConfigurableConversionService conversionService;
	
	public boolean lenient;
	
	public String  datePattern;

	@SuppressWarnings("unchecked")
	TupleSerializationValues(DefaultTuple tuple) {
		this(new LinkedList(tuple.getFieldNames()), new LinkedList<>(tuple.getValues()) ,tuple.getDateFormat(),
				tuple.getLocale(), tuple.getConversionService());
	}

	@SuppressWarnings("unchecked")
	TupleSerializationValues(List<String> names, List<Object> values, DateFormat dateFormat, Locale locale,
			ConfigurableConversionService configurableConversionService) {
		this.names = names;
		this.values = values;
		if (dateFormat instanceof SimpleDateFormat) {
			this.dateFormat = null;
			SimpleDateFormat simpleDateFormat = (SimpleDateFormat) dateFormat;
			this.datePattern = simpleDateFormat.toPattern();
		}
		
		this.lenient = dateFormat.isLenient();
		this.locale = locale;
		if (! (configurableConversionService instanceof DefaultTupleConversionService)) {
			this.conversionService = configurableConversionService;
		}
	}

	boolean isDefaultDateFormat() {
			return (this.dateFormat == null) && this.datePattern.equals(TupleBuilder.DEFAULT_DATE_PATTERN);
	}
	
	boolean isDefaultLocale() {
		return locale.equals(TupleBuilder.DEFAULT_LOCALE);
	}

	boolean isDefaultConversionService() {
		return this.conversionService == null;
	}
	
	DateFormat getDateFormat() {
		DateFormat df = null;
		if (this.dateFormat == null) {
			SimpleDateFormat sdf = new SimpleDateFormat(this.datePattern);
			sdf.setLenient(this.lenient);
			df = sdf;
		}
		else {
			df = this.dateFormat;
		}
		
		return df;
		
	}
}
