/*
 * Copyright 2013-2015 the original author or authors.
 *
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.Assert;
import org.springframework.util.IdGenerator;

/**
 * Builder class to create Tuple instances.
 * Default Locale is US for NumberFormat and default DatePattern is "yyyy-MM-dd"
 * <em>Note:</em> Using a custom conversion service that is instance based (not configured
 * as a singleton) will have <em>significant</em> performance impacts.
 * @author Mark Pollack
 * @author David Turanski
 * @author Michael Minella
 * @author Gunnar Hillert
 */
public class TupleBuilder {

	private List<String> names = new ArrayList<>();

	private List<Object> values = new ArrayList<>();

	private UUID id;

	private Long timestamp;

	private static TupleConversionServiceWrapper defaultConversionServiceWrapper;

	private TupleConversionServiceWrapper customConversionServiceWrapper = null;

	public final static String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

	public final static Locale DEFAULT_LOCALE = Locale.US;

	private static Converter<Tuple, String> tupleToStringConverter = new TupleToJsonStringConverter();

	private static Converter<String, Tuple> stringToTupleConverter = new JsonStringToTupleConverter();

	private static final IdGenerator defaultIdGenerator = new AlternativeJdkIdGenerator();

	static {
		ConfigurableConversionService configurableConversionService = new DefaultTupleConversionService();

		configurableConversionService.addConverterFactory(new LocaleAwareStringToNumberConverterFactory(NumberFormat
				.getInstance(DEFAULT_LOCALE)));
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
		dateFormat.setLenient(false);
		configurableConversionService.addConverter(new StringToDateConverter(dateFormat));
		defaultConversionServiceWrapper = new TupleConversionServiceWrapper(configurableConversionService);
		defaultConversionServiceWrapper.setDateFormat(dateFormat);
		defaultConversionServiceWrapper.setLocale(DEFAULT_LOCALE);
	}

	public static TupleBuilder tuple() {
		return new TupleBuilder();
	}

	public Tuple of(String k1, Object v1) {
		return newTuple(namesOf(k1), valuesOf(v1));
	}

	public Tuple of(String k1, Object v1, String k2, Object v2) {
		addEntry(k1, v1);
		addEntry(k2, v2);
		return build();
	}

	public Tuple of(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
		addEntry(k1, v1);
		addEntry(k2, v2);
		addEntry(k3, v3);
		return build();
	}

	public Tuple of(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
		addEntry(k1, v1);
		addEntry(k2, v2);
		addEntry(k3, v3);
		addEntry(k4, v4);
		return build();
	}

	public Tuple ofNamesAndValues(List<String> names, List<Object> values) {
		this.names = names;
		this.values = values;
		return build();
	}

	public TupleBuilder put(String k1, Object v1) {
		addEntry(k1, v1);
		return this;
	}

	public Tuple build() {
		return newTuple(names, values);
	}

	public static Tuple fromString(String source) {
		return stringToTupleConverter.convert(source);
	}

	public TupleBuilder setConfigurableConversionService(ConfigurableConversionService formattingConversionService) {
		Assert.notNull(formattingConversionService);
		this.customConversionServiceWrapper = new TupleConversionServiceWrapper(formattingConversionService);
		return this;
	}

	public ConversionServiceBuilder setFormats(Locale locale, DateFormat dateFormat) {
		return new ConversionServiceBuilder(this, locale, dateFormat);
	}
	
	public TupleBuilder addId() {
		addId(defaultIdGenerator.generateId());
		return  this;
	}

	public TupleBuilder addId(UUID id) {
		Assert.notNull(id, "'id' cannot be null");
		this.id = id;
		return  this;
	}
	
	public TupleBuilder addTimestamp() {
		this.timestamp = System.currentTimeMillis();
		return this;
	}

	void addEntry(String k1, Object v1) {
		names.add(k1);
		values.add(v1);
	}

	static List<Object> valuesOf(Object v1) {
		ArrayList<Object> values = new ArrayList<>();
		values.add(v1);
		return Collections.unmodifiableList(values);
	}

	static List<String> namesOf(String k1) {
		List<String> fields = new ArrayList<>(1);
		fields.add(k1);
		return Collections.unmodifiableList(fields);

	}

	protected Tuple newTuple(List<String> names, List<Object> values) {
		DefaultTuple tuple = new DefaultTuple(names, values, customConversionServiceWrapper == null ? 
				defaultConversionServiceWrapper : customConversionServiceWrapper, id, timestamp);
		tuple.setTupleToStringConverter(tupleToStringConverter);
		return tuple;
	}

	/**
	 * Provides the ability to inject a {@link ConfigurableConversionService} as a way to
	 * customize conversion behavior in the built {@link Tuple}.
	 * @author Michael Minella
	 * @author David Turanski
	 */
	public static class ConversionServiceBuilder {

		private TupleBuilder builder;

		private Locale locale;

		private DateFormat dateFormat;

		ConversionServiceBuilder(TupleBuilder builder, Locale locale, DateFormat dateFormat) {
			this.builder = builder;
			this.locale = locale;
			this.dateFormat = dateFormat;
		}

		public TupleBuilder setConfigurableConversionService(ConfigurableConversionService 
				formattingConversionService) {
			Assert.notNull(formattingConversionService);
			builder.setConfigurableConversionService(formattingConversionService);

			if (locale != null) {
				builder.customConversionServiceWrapper.setLocale(locale);
			}

			if (dateFormat != null) {
				builder.customConversionServiceWrapper.setDateFormat(dateFormat);
			}

			return builder;
		}
	}
}
