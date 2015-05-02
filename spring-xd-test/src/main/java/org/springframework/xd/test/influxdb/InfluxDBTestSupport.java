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

package org.springframework.xd.test.influxdb;

import org.springframework.xd.test.AbstractExternalResourceTestSupport;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

public class InfluxDBTestSupport extends AbstractExternalResourceTestSupport<InfluxDB> {

    public InfluxDBTestSupport() {
        super("InfluxDB");
    }

    @Override
    protected void cleanupResource() throws Exception {
    }

    @Override
    protected void obtainResource() throws Exception {
        resource = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        resource.createDatabase("foobar");
    }
}
