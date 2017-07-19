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
 */
package org.springframework.amqp.tutorials.tut6;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Gary Russell
 * @author Scott Deeg
 */
public class Tut6Client {

	@Autowired
	@Qualifier("fibTemplate")
	private RabbitTemplate fibTemplate;

	@Autowired
	@Qualifier("squareTemplate")
	private RabbitTemplate squareTemplate;

	@Autowired
	@Qualifier("fibExchange")
	private DirectExchange fibExchange;

	@Autowired
	@Qualifier("squareExchange")
	private DirectExchange squareExchange;

	int start = 0;

	@Scheduled(fixedDelay = 1000, initialDelay = 500)
	public void send() {
		int nxt = start++;
		System.out.println(" [x] Requesting fib(" + nxt + ")");
		Integer fibResponse = (Integer) fibTemplate.convertSendAndReceive(fibExchange.getName(), "rpc", nxt);
		System.out.println(" [.] Got '" + fibResponse + "'");
		System.out.println(" [x] Requesting square(" + nxt + ")");
		Integer squareResponse = (Integer) squareTemplate.convertSendAndReceive(squareExchange.getName(), "rpc", nxt);
		System.out.println(" [.] Got '" + squareResponse+ "'");


	}

}
