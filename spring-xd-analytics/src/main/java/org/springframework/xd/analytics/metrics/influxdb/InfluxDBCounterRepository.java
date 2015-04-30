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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.xd.analytics.metrics.core.Counter;
import org.springframework.xd.analytics.metrics.core.CounterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An InfluxDB backed counter repository.
 *
 * @author Eric Bottard
 */
public class InfluxDBCounterRepository extends AbstractInfluxDBMetricRepository<Counter> implements CounterRepository {

    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        System.out.println(influxDB.ping());

        List<Serie> foobar = influxDB.query("foobar", "select * from foot limit 1", TimeUnit.MILLISECONDS);
        System.out.println(foobar.size());
        System.out.println(foobar);
        System.out.println(foobar.iterator().next().getRows().get(0).keySet());
    }

    public InfluxDBCounterRepository() {
        super("counter", "http://localhost:8086", "root", "root", "foobar");
    }


    @Override
    public long increment(String name) {
        return increment(name, 1L);
    }

    @Override
    public long increment(String name, long amount) {
        Serie serie = new Serie.Builder(seriesName(name)).columns("increment").values(amount).build();
        influxDB.write(dbName, TimeUnit.MILLISECONDS, serie);
        return findOne(name).getValue();
    }


    @Override
    public long decrement(String name) {
        return increment(name, -1L);
    }

    @Override
    public void reset(String name) {
        //TODO: reset value to 0, do not delete
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
        } else {
            increment(name, entity.getValue() - c.getValue());
        }
        return entity;
    }

    @Override
    public Counter findOne(String name) {
        List<Serie> series = safeQuery("select sum(increment) from %s", seriesName(name));
        Double sum = singleScalar(series, "sum");
        //TODO: for consistency (w.r.t. other impl), return null when 0 (reset)?
        return sum == null ? null : new Counter(name, sum.longValue());
    }

    @Override
    public Iterable<Counter> findAll() {
        List<Serie> series = safeQuery("select sum(increment) from %s", all());
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
        return null;
    }
}
