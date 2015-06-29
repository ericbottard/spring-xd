package org.springframework.xd.analytics.metrics.influxdb;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.xd.analytics.metrics.AbstractRichGaugeRepositoryTests;
import org.springframework.xd.analytics.metrics.common.InfluxDBRepositoriesConfig;
import org.springframework.xd.analytics.metrics.core.RichGaugeRepository;
import org.springframework.xd.test.influxdb.InfluxDBTestSupport;

/**
 */
@ContextConfiguration(classes = InfluxDBRepositoriesConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InfluxDBRichGaugeRepositoryTests extends AbstractRichGaugeRepositoryTests {

	@Autowired
	InfluxDBRichGaugeRepository richGaugeRepository;

	@Rule
	public InfluxDBTestSupport influxDBAvailableRule = new InfluxDBTestSupport();

	@Before
	@After
	public void cleanUp() {
		richGaugeRepository.deleteAll();
	}

	@Test
	@Ignore("Exponential moving avg not implemented")
	public void testExponentialMovingAverage() throws Exception {
		super.testExponentialMovingAverage();
	}

	@Override
	protected RichGaugeRepository createService() {
		return richGaugeRepository;
	}
}