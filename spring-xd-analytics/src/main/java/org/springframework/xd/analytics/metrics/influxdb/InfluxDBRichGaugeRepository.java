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
import org.springframework.xd.analytics.metrics.core.RichGauge;
import org.springframework.xd.analytics.metrics.core.RichGaugeRepository;

/**
 * An InfluxDB backed rich gauge repository.
 *
 * @author Eric Bottard
 */
public class InfluxDBRichGaugeRepository extends AbstractInfluxDBMetricRepository<RichGauge>
										 implements RichGaugeRepository {

	private static final String VALUE_COLUMN = "value";

	public InfluxDBRichGaugeRepository(String url, String username, String password, String dbName) {
		super("rich_gauge", url, username, password, dbName, Arrays.asList("time", "sequence_number"));
	}

	public InfluxDBRichGaugeRepository() {
		this("http://localhost:8086", "root", "root", "foobar");
	}

	@Override
	public RichGauge save(RichGauge entity) {
		Serie serie = new Serie.Builder(seriesName(entity.getName()))
				.columns(VALUE_COLUMN).values(entity.getValue()).build();
		influxDB.write(dbName, TimeUnit.MILLISECONDS, serie);
		return entity;
	}

	@Override
	public RichGauge findOne(String name) {
		Assert.hasText(name, "The name of the RichGaugeCounter must not be blank");

		List<Serie> series = safeQuery(
				"SELECT FIRST(%s) as val, MEAN(%s) as average, MAX(%s) as max, MIN(%s) as min, COUNT(%s) as count FROM %s LIMIT 1",
				VALUE_COLUMN,
				VALUE_COLUMN,
				VALUE_COLUMN,
				VALUE_COLUMN,
				VALUE_COLUMN,
				seriesName(name)
		);

		Serie serie = singleSerie(series);
		return new RichGauge(name,
				singleScalar(serie, "val"),
				-1d,
				singleScalar(serie, "average"),
				singleScalar(serie, "max"),
				singleScalar(serie, "min"),
				singleScalar(serie, "count").longValue()
		);
	}

	@Override
	public Iterable<RichGauge> findAll() {
		List<Serie> series = safeQuery(
				"SELECT FIRST(%s) as val, MEAN(%s) as average, MAX(%) as max, MIN(%s) as min, COUNT(%s) as count FROM %s LIMIT 1",
				VALUE_COLUMN,
				VALUE_COLUMN,
				VALUE_COLUMN,
				VALUE_COLUMN,
				VALUE_COLUMN,
				all()
		);

		List<RichGauge> results = new ArrayList<>(series.size());
		for (Serie serie : series) {
			results.add(
					new RichGauge(serie.getName(),
							singleScalar(serie, "val"),
							-1d,
							singleScalar(serie, "average"),
							singleScalar(serie, "max"),
							singleScalar(serie, "min"),
							singleScalar(serie, "count").longValue()
					)
			);
		}
		return results;
	}

	@Override
	public void recordValue(String name, double value, double alpha) {
		save(new RichGauge(name, value));
	}

	@Override
	public void reset(String name) {
		save(new RichGauge(name, 0));
	}
}
