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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.influxdb.dto.Serie;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import org.springframework.xd.analytics.metrics.core.AggregateCount;
import org.springframework.xd.analytics.metrics.core.AggregateCountResolution;
import org.springframework.xd.analytics.metrics.core.AggregateCounterRepository;

/**
 * An InfluxDB backed aggregate counter repository.
 *
 * @author Eric Bottard
 */
public class InfluxDBAggregateCounterRepository extends InfluxDBCounterRepository implements AggregateCounterRepository {

	public InfluxDBAggregateCounterRepository() {
		this("http://localhost:8086", "root", "root", "foobar");
	}

	public InfluxDBAggregateCounterRepository(String url, String username, String password, String dbName) {
		super("aggregate_counter", url, username, password, dbName, Arrays.asList(TIME_COLUMN, SEQUENCE_COLUMN));
	}


	@Override
	public AggregateCount getCounts(String name, int nCounts, AggregateCountResolution resolution) {
		return null;
	}

	@Override
	public AggregateCount getCounts(String name, int nCounts, DateTime end, AggregateCountResolution resolution) {
		return null;
	}

	@Override
	public AggregateCount getCounts(String name, Interval interval, AggregateCountResolution resolution) {
		String bucket = toBucket(resolution);
		String start = interval.getStart().getMillis() + "u";
		String end = interval.getEnd().getMillis() + "u";
		List<Serie> series = safeQuery("select sum(%s) as sum from %s where time > %s and time < %s group by time(%s) fill(0)",
				INCREMENT_COLUMN,
				seriesName(name),
				start,
				end,
				bucket
		);
		Serie serie = singleSerie(series);
		int i = 0;
		long[] counts = new long[serie.getRows().size()];
		for (Map<String, Object> row : serie.getRows()) {
			counts[i++] = ((Double)row.get("sum")).longValue();
		}
		return new AggregateCount(name, interval, counts, resolution);
	}

	private String toBucket(AggregateCountResolution resolution) {
		switch (resolution) {
			case day:
				return "1d";
			case hour:
				return "1h";
			case minute:
				return "1m";
			case month:
				return "30.5d";
			case year:
				return "365d";
			default:
				throw new AssertionError("Unexpected enum value");
		}

	}

}