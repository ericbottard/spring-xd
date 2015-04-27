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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.springframework.xd.tuple.DefaultTuple;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleBuilder;

/**
 * @author David Turanski
 */
public class DefaultTupleSerializer extends Serializer<Tuple> {
	@Override
	public void write(Kryo kryo, Output output, Tuple object) {
		TupleSerializationValues tupleSerializationValues = new TupleSerializationValues((DefaultTuple) object);
		kryo.writeObject(output, tupleSerializationValues);
	}

	@Override
	public Tuple read(Kryo kryo, Input input, Class<Tuple> type) {
		TupleSerializationValues tupleSerializationValues = kryo.readObject(input, TupleSerializationValues.class);
		TupleBuilder tupleBuilder = TupleBuilder.tuple();
		if (!tupleSerializationValues.isDefaultConversionService()) {
			tupleBuilder.setConfigurableConversionService(tupleSerializationValues.conversionService);
		}
		if (!(tupleSerializationValues.isDefaultDateFormat() && tupleSerializationValues.isDefaultLocale())) {
			tupleBuilder.setFormats(tupleSerializationValues.locale, tupleSerializationValues.getDateFormat());
		}

		Tuple tuple = TupleBuilder.tuple().ofNamesAndValues(tupleSerializationValues.names, 
				tupleSerializationValues.values);
		return tuple;
	}
}
