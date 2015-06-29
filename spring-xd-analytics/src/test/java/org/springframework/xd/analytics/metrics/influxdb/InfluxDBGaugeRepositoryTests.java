package org.springframework.xd.analytics.metrics.influxdb;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.xd.analytics.metrics.AbstractCounterRepositoryTests;
import org.springframework.xd.analytics.metrics.SharedGaugeRepositoryTests;
import org.springframework.xd.analytics.metrics.common.InfluxDBRepositoriesConfig;
import org.springframework.xd.test.influxdb.InfluxDBTestSupport;

/**
 */
@ContextConfiguration(classes = InfluxDBRepositoriesConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InfluxDBGaugeRepositoryTests extends SharedGaugeRepositoryTests {


	@Rule
	public InfluxDBTestSupport influxDBAvailableRule = new InfluxDBTestSupport();

	@Before
	@After
	public void cleanUp() {
		gaugeRepository.deleteAll();
	}

}