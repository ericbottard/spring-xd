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

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.joda.time.DateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.xd.analytics.metrics.core.Counter;
import org.springframework.xd.analytics.metrics.core.CounterRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An InfluxDB backed counter repository.
 *
 * @author Eric Bottard
 */
public class InfluxDBCounterRepository extends AbstractInfluxDBMetricRepository<Counter> implements CounterRepository {

	protected final static String INCREMENT_COLUMN = "increment";

	public InfluxDBCounterRepository() {
		this("http://localhost:8086", "root", "root", "foobar");
	}

	public InfluxDBCounterRepository(String url, String username, String password, String dbName) {
		super("counter", url, username, password, dbName, Arrays.asList(TIME_COLUMN, SEQUENCE_COLUMN));
	}

	protected InfluxDBCounterRepository(String prefix, String url, String username, String password, String dbName, List<String> reservedColumns) {
		super(prefix, url, username, password, dbName, reservedColumns);
	}

	@Override
	public long increment(String name) {
		return increment(name, 1L);
	}

	@Override
	public long increment(String name, long amount) {
		return increment(name, amount, DateTime.now());
	}

	public long increment(String name, long amount, DateTime dateTime) {
		Serie serie = new Serie.Builder(seriesName(name))
				.columns(INCREMENT_COLUMN, "time")
				.values(amount, dateTime.getMillis())
				.build();
		influxDB.write(dbName, TimeUnit.MILLISECONDS, serie);
		return findOne(name).getValue();
	}


	@Override
	public long decrement(String name) {
		return increment(name, -1L);
	}

	@Override
	public void reset(String name) {
		delete(name);
	}

	@Override
	public Iterable<Counter> findAll(Sort sort) {
		return null;
	}

	@Override
	public Page<Counter> findAll(Pageable pageable) {
		return null;
	}

	@Override
	public Counter save(Counter entity) {
		// A bit hacky to compute delta, but does not matter as XD handler uses increment, not save
		String name = entity.getName();
		Counter c = findOne(name);
		if (c == null) {
			increment(name, entity.getValue());
		}
		else {
			increment(name, entity.getValue() - c.getValue());
		}
		return entity;
	}

	@Override
	public Counter findOne(String name) {
		Assert.notNull(name, "name cannot be null");
		List<Serie> series = safeQuery("select sum(%s) as sum from %s", INCREMENT_COLUMN, seriesName(name));
		Double sum = singleScalar(series, "sum");
		return sum == null ? null : new Counter(name, sum.longValue());
	}

	@Override
	public Iterable<Counter> findAll() {
		List<Serie> series = safeQuery("select sum(%s) as sum from %s", INCREMENT_COLUMN, all());
		List<Counter> result = new ArrayList<>(series.size());
		for (Serie serie : series) {
			String name = metricName(serie.getName());
			long value = singleScalar(serie, "sum").longValue();
			result.add(new Counter(name, value));
		}
		return result;
	}

	@Override
	public Iterable<Counter> findAllInRange(String from, boolean fromInclusive, String to, boolean toInclusive) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
