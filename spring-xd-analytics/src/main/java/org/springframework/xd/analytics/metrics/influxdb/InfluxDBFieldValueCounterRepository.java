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

import org.influxdb.dto.Serie;
import org.springframework.util.Assert;
import org.springframework.xd.analytics.metrics.core.FieldValueCounter;
import org.springframework.xd.analytics.metrics.core.FieldValueCounterRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * An InfluxDB backed field value counter repository.
 *
 * @author Eric Bottard
 * @author Florent Biville
 */
public class InfluxDBFieldValueCounterRepository extends AbstractInfluxDBMetricRepository<FieldValueCounter> implements FieldValueCounterRepository {

    public InfluxDBFieldValueCounterRepository() {
        super("field-value-counter", "http://localhost:8086", "root", "root", "foobar");
    }

    @Override
    public void increment(String name, String fieldName) {
        Assert.hasText(name, "The name of the FieldValueCounter must not be blank");
        Assert.hasText(fieldName, "The field name of the FieldValueCounter must not be blank");

        writeValue(name, fieldName, 1);
    }

    @Override
    public void decrement(String name, String fieldName) {
        Assert.hasText(name, "The name of the FieldValueCounter must not be blank");
        Assert.hasText(fieldName, "The field name of the FieldValueCounter must not be blank");

        writeValue(name, fieldName, -1);
    }

    @Override
    public void reset(String name, String fieldName) {
        Assert.hasText(name, "The name of the FieldValueCounter must not be blank");
        Assert.hasText(fieldName, "The field name of the FieldValueCounter must not be blank");

        Double currentValue = findOne(name).getFieldValueCount().get(fieldName);
        writeValue(name, fieldName, -1 * currentValue);
    }

    @Override
    public FieldValueCounter save(FieldValueCounter entity) {
        String name = entity.getName();

        Set<String> fields = entity.getFieldValueCount().keySet();
        Map<String, Double> counters = updatedCounters(
                entity,
                fields,
                currentSumsSerie(name, fields)
        );

        Serie serie = new Serie.Builder(seriesName(name))
                .columns(fields.toArray(new String[fields.size()]))
                .values(valuesToSave(fields, counters))
                .build();

        influxDB.write(dbName, TimeUnit.MILLISECONDS, serie);

        return new FieldValueCounter(name, counters);
    }

    @Override
    public FieldValueCounter findOne(String name) {
        Assert.hasText(name, "The name of the FieldValueCounter must not be blank");

        Serie columns = singleSerie(safeQuery("select * from %s limit 1", seriesName(name)));
        Serie serie = currentSumsSerie(name, aggregateColumns(columns));

        return new FieldValueCounter(name, asMap(serie));
    }

    @Override
    public Iterable<FieldValueCounter> findAll() {
        Collection<FieldValueCounter> results = new ArrayList<>();
        List<Serie> series = safeQuery("select * from %s limit 1", all());
        for (Serie serie : series) {
            results.add(findOne(metricName(serie.getName())));
        }
        return results;
    }

    private <T extends Number> void writeValue(String name, String fieldName, T value) {
        Serie serie = new Serie.Builder(seriesName(name)).columns(fieldName).values(value).build();
        influxDB.write(dbName, TimeUnit.MILLISECONDS, serie);
    }

    private Serie currentSumsSerie(String name, Set<String> fields) {
        StringBuilder from = new StringBuilder();
        for (String field : fields) {
            from.append(format("sum(%s) AS %s,", field, field));
        }
        String sums = from.substring(0, from.length() - 1);
        return singleSerie(safeQuery("select %s from %s", sums, seriesName(name)));
    }

    private Map<String, Double> updatedCounters(FieldValueCounter entity, Set<String> fields, Serie previousSerie) {
        Map<String, Double> result = new HashMap<>();
        for (String field : fields) {
            Map<String, Object> row = singleRow(previousSerie);
            result.put(field, currentOrDelta(field, entity, row));
        }
        return result;
    }

    private Double[] valuesToSave(Set<String> fields, Map<String, Double> counters) {
        List<Double> values = new ArrayList<>();
        for (String field : fields) {
            values.add(counters.get(field));
        }
        return values.toArray(new Double[fields.size()]);
    }

    private Set<String> aggregateColumns(Serie serie) {
        Set<String> result = new LinkedHashSet<>();
        for (String column : asList(serie.getColumns())) {
            if (!RESERVED_COLUMNS.contains(column)) {
                result.add(column);
            }
        }
        return result;
    }

    private Map<String, Double> asMap(Serie serie) {
        Map<String, Double> result = new HashMap<>();
        Map<String, Object> row = singleRow(serie);
        for (Map.Entry<String, Object> column: row.entrySet()) {
            String columnName = column.getKey();
            if (!RESERVED_COLUMNS.contains(columnName)) {
                result.put(columnName, (Double) column.getValue());
            }
        }
        return result;
    }

    private Double currentOrDelta(String field, FieldValueCounter entity, Map<String, Object> previous) {
        Double previousValue = (Double) previous.get(field);
        Double currentValue = entity.getFieldValueCount().get(field);
        if (previousValue == null) {
            return currentValue;
        }
        return currentValue - previousValue;
    }
}
