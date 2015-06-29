/*
 * Copyright 2015 the original author or authors.
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
 *
 *
 */

package org.springframework.xd.analytics.metrics.influxdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Serie;

import org.springframework.util.Assert;
import org.springframework.xd.analytics.metrics.core.Gauge;
import org.springframework.xd.analytics.metrics.core.GaugeRepository;

/**
 * An InfluxDB gauge repository.
 *
 * @author Eric Bottard
 */
public class InfluxDBGaugeRepository extends AbstractInfluxDBMetricRepository<Gauge>
implements GaugeRepository {

	private static final String VALUE_COLUMN = "value";

	public InfluxDBGaugeRepository() {
		this("http://localhost:8086", "root", "root", "foobar");
	}

	public InfluxDBGaugeRepository( String url, String username, String password, String dbName) {
		super("gauge", url, username, password, dbName, Arrays.asList("time", "sequence_number"));
	}

	@Override
	public Gauge save(Gauge entity) {
		Serie serie = new Serie.Builder(seriesName(entity.getName()))
				.columns(VALUE_COLUMN).values(entity.getValue()).build();
		influxDB.write(dbName, TimeUnit.MILLISECONDS, serie);
		return entity;
	}

	@Override
	public Gauge findOne(String name) {
		Assert.hasText(name, "The name of the GaugeCounter must not be blank");

		List<Serie> series = safeQuery("SELECT %s FROM %s LIMIT 1", VALUE_COLUMN, seriesName(name));
		if (series == null) {
			return null;
		}
		Double value = singleScalar(series, VALUE_COLUMN);
		return new Gauge(name, value.longValue());
	}

	@Override
	public Iterable<Gauge> findAll() {
		List<Serie> series = safeQuery("select %s from %s", VALUE_COLUMN, all());
		List<Gauge> result = new ArrayList<>(series.size());
		for (Serie serie : series) {
			String name = serie.getName();
			Double value = singleScalar(serie, VALUE_COLUMN);
			result.add(new Gauge(name, value.longValue()));
		}
		return result;
	}

	@Override
	public void recordValue(String name, long value) {
		save(new Gauge(name, value));
	}

	@Override
	public void reset(String name) {
		recordValue(name, 0L);
	}
}
