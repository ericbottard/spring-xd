/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.analytics.metrics.common;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.xd.analytics.metrics.influxdb.InfluxDBFieldValueCounterRepository;

/**
 * Provides InfluxDB backed repositories, to be tested one by one in InfluxDB variant of tests.
 *
 * @author Florent Biville
 */
@Configuration
public class InfluxDBRepositoriesConfig {

    @Bean
    public InfluxDBFieldValueCounterRepository influxDBFieldValueCounterRepository() {
        return new InfluxDBFieldValueCounterRepository();
    }
}
