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

package org.springframework.xd.store;

import org.springframework.xd.analytics.metrics.core.Metric;
import org.springframework.xd.analytics.metrics.core.MetricRepository;

/**
 * Abstract base class for InfluxDB repositories.
 *
 * @author Eric Bottard
 */
public class AbstractInfluxDBMetricRepository<M extends Metric> implements MetricRepository<M>{
    @Override
    public <S extends M> S save(S entity) {
        return null;
    }

    @Override
    public <S extends M> Iterable<S> save(Iterable<S> entities) {
        return null;
    }

    @Override
    public M findOne(String s) {
        return null;
    }

    @Override
    public boolean exists(String s) {
        return false;
    }

    @Override
    public Iterable<M> findAll() {
        return null;
    }

    @Override
    public Iterable<M> findAll(Iterable<String> strings) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(String s) {

    }

    @Override
    public void delete(M entity) {

    }

    @Override
    public void delete(Iterable<? extends M> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
