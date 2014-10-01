/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.xd.analytics.metrics.integration;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.expression.IntegrationEvaluationContextAware;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;


/**
 *
 */
abstract class AbstractMetricHandler implements IntegrationEvaluationContextAware {

	protected final Expression nameExpression;

	protected EvaluationContext evaluationContext = new StandardEvaluationContext();

	protected SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

	protected AbstractMetricHandler(String nameExpression) {
		Assert.notNull(nameExpression, "Metric name expression can not be null");
		this.nameExpression = spelExpressionParser.parseExpression(nameExpression);
	}

	protected String computeMetricName(Message<?> message) {
		String counterName = nameExpression.getValue(evaluationContext, message, CharSequence.class).toString();
		return counterName;
	}

	@Override
	public void setIntegrationEvaluationContext(EvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}

}
