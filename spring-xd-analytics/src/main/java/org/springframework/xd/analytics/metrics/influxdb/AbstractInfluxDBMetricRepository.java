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

import org.springframework.util.Assert;
import org.springframework.xd.analytics.metrics.core.Metric;
import org.springframework.xd.analytics.metrics.core.MetricRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Base class for metric repositories that write to Influx DB.
 *
 * <p>Most of the implementations in this package simply write data points to Influx DB,
 * leveraging its time series functionality to achieve Spring XD metric semantics. For example,
 * simple gauge and rich gauge are stored in the same way.</p>
 *
 * @author Eric Bottard
 */
/*default*/ abstract class AbstractInfluxDBMetricRepository<M extends Metric> implements MetricRepository<M> {
	protected String dbName;

	protected InfluxDB influxDB;

	private String prefix;

	protected static final List<String> RESERVED_COLUMNS = Arrays.asList("time", "sequence_number");

	protected AbstractInfluxDBMetricRepository(String prefix, String url, String username, String password, String dbName) {
		this.prefix = prefix;
		this.dbName = dbName;
		this.influxDB = InfluxDBFactory.connect(url, username, password);
	}

	@Override
	public boolean exists(String s) {
		return safeQuery("select time from %s limit 1", seriesName(s)) != null;
	}

	@Override
	public long count() {
		return safeQuery("select time from %s limit 1", all()).size();
	}

	@Override
	public void delete(String name) {
		influxDB.deleteSeries(dbName, seriesName(name));
	}


	@Override
	public void delete(M entity) {
		delete(entity.getName());
	}

	@Override
	public void delete(Iterable<? extends M> entities) {
		for (M m : entities) {
			delete(m);
		}
	}

	@Override
	public void deleteAll() {
		influxDB.deleteSeries(dbName, all());
	}

	@Override
	public Iterable<M> findAll(Iterable<String> strings) {
		List<M> result = new ArrayList<>();
		for (String name : strings) {
			result.add(findOne(name));
		}
		return result;
	}

	@Override
	public <S extends M> Iterable<S> save(Iterable<S> entities) {
		for (S entity : entities) {
			save(entity);
		}
		return entities;
	}

	/**
	 * Return the name of the Influx DB series that should be used to store the Spring XD metric.
	 */
	protected String seriesName(String metricName) {
		String name = prefix + "." + metricName;
		return String.format("\"%s\"", name.replace("\"", "\\\""));
	}

	/**
	 * Return the regex that matches all series for this class kind of metrics.
	 */
	protected String all() {
		return "/" + prefix + "..*" + "/";
	}

	/**
	 * Return the name of the Spring XD metric that corresponds to an Influx DB series name.
	 */
	protected String metricName(String seriesName) {
		return seriesName.substring((prefix + ".").length());
	}

	/**
	 * Run a query against the current database, returning null in case of series not found exception.
	 * Supports {@link String#format(String, Object...)} syntax in the query as a convenience.
	 */
	protected List<Serie> safeQuery(String query, Object... params) {
		try {
			return influxDB.query(dbName, String.format(query, params), TimeUnit.MICROSECONDS);
		}
		catch (RuntimeException e) {
			if (e.getMessage().startsWith("Couldn't find series: ")) {
				return null;
			}
			else {
				throw e;
			}
		}
	}

	protected Double singleScalar(List<Serie> series, String column) {
		if (series == null) {
			return null;
		}
		Map<String, Object> row = singleRow(series);
		return (Double) row.get(column);
	}

	protected Serie singleSerie(List<Serie> series) {
		Assert.state(series.size() == 1, "Expected a single series result, but result had " + series.size());
		return series.get(0);
	}

	protected Map<String, Object> singleRow(Serie serie) {
		List<Map<String, Object>> rows = serie.getRows();
		Assert.state(rows.size() == 1, "Expected a single row result, but result had " + rows.size());
		return rows.get(0);
	}
	protected Map<String, Object> singleRow(List<Serie> series) {
		if (series == null) {
			return null;
		}
		List<Map<String, Object>> rows = singleSerie(series).getRows();
		return singleRow(singleSerie(series));
	}

	protected Double singleScalar(Serie serie, String column) {
		return (Double) singleRow(serie).get(column);
	}
}
