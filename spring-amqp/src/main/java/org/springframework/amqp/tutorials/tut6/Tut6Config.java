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

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.common.AmqpServiceInfo;
import org.springframework.context.annotation.*;

/**
 * @author Gary Russell
 * @author Scott Deeg
 *
 */
@Profile({"tut6","rpc"})
@Configuration
@PropertySource("multiple.properties")
public class Tut6Config {

	@Value("${rabbit1.host}")
	private String rabbit1Host;

	@Value("${rabbit1.port}")
	private int rabbit1Port;

	@Value("${rabbit2.host}")
	private String rabbit2Host;

	@Value("${rabbit2.port}")
	private int rabbit2Port;

	@Value("${rabbit3.host}")
	private String rabbit3Host;

	@Value("${rabbit3.port}")
	private int rabbit3Port;


	@Bean(name="r1cf")
	@Profile("!cloud")
	public ConnectionFactory r1cf(){
		return new CachingConnectionFactory(rabbit1Host,rabbit1Port);
	}

	@Profile("!cloud")
	@Bean(name="r2cf")
	public ConnectionFactory r2cf(){
		return new CachingConnectionFactory(rabbit2Host,rabbit2Port);
	}

	@Profile("!cloud")
	@Bean(name="rabbitConnectionFactory")
	public ConnectionFactory r3cf(){
		return new CachingConnectionFactory(rabbit3Host,rabbit3Port);
	}



	private ConnectionFactory getCloudCF(String name){
		CloudFactory cloudFactory = new CloudFactory();
		Cloud cloud = cloudFactory.getCloud();
		AmqpServiceInfo serviceInfo = (AmqpServiceInfo) cloud.getServiceInfo(name);
		String serviceID = serviceInfo.getId();
		return cloud.getServiceConnector(serviceID, ConnectionFactory.class, null);
	}

	@Bean(name="r1cf")
	@Profile("cloud")
	public ConnectionFactory r1cfCloud(){
		return getCloudCF("rabbit1");
	}

	@Profile("cloud")
	@Bean(name="r2cf")
	public ConnectionFactory r2cfCloud(){
		return getCloudCF("rabbit2");
	}

	@Profile("cloud")
	@Bean(name="rabbitConnectionFactory")
	public ConnectionFactory r3cfCloud(){
		return getCloudCF("rabbit3");
	}




	@Bean
	@Primary
	public RabbitTemplate defaultTemplate() {
		return new RabbitTemplate(r3cf());
	}

	@Bean(name="rabbitListenerContainerFactory")
	public RabbitListenerContainerFactory rlcf1() {
		SimpleRabbitListenerContainerFactory ret = new SimpleRabbitListenerContainerFactory();
		ret.setConnectionFactory(r1cf());
		return ret;
	}


	@Bean(name="fibExchange")
	public DirectExchange fibExchange() {
		DirectExchange de =  new DirectExchange("tut.rpc.fib");
		de.setAdminsThatShouldDeclare(rabbit1Admin());
		return de;
	}

	@Bean(name="squareExchange")
	public DirectExchange squareExchange() {
		DirectExchange de =  new DirectExchange("tut.rpc.square");
		de.setAdminsThatShouldDeclare(rabbit2Admin());
		return de;
	}

	@Bean
	public RabbitAdmin rabbit1Admin(){
		RabbitAdmin ret = new RabbitAdmin(r1cf());
		ret.afterPropertiesSet();
		return ret;
	}

	@Bean
	public RabbitAdmin rabbit2Admin(){
		RabbitAdmin ret = new RabbitAdmin(r2cf());
		ret.afterPropertiesSet();
		return ret;
	}


	@Profile("!cloud")
	private class NonCloudConfig{

	}

	@Profile("client")
	private class ClientConfig {

		@Bean(name="fibTemplate")
		public RabbitTemplate fibTemplate() {
			return new RabbitTemplate(r1cf());
		}

		@Bean(name="squareTemplate")
		public RabbitTemplate squareTemplate() {
			return new RabbitTemplate(r2cf());
		}

		@Bean
		public Tut6Client client() {
	 	 	return new Tut6Client();
		}

	}


	@Profile("server")
	private class ServerConfig {

		@Bean(name="fibQueue")
		public Queue fibQueue() {
			Queue q = new Queue("tut.rpc.requests.fib");
			q.setAdminsThatShouldDeclare(rabbit1Admin());
			return q;
		}

		@Bean(name="squareQueue")
		public Queue squareQueue() {
			Queue q =  new Queue("tut.rpc.requests.square");
			q.setAdminsThatShouldDeclare(rabbit2Admin());
			return q;
		}


		@Bean
		public Binding fibBinding(@Qualifier("fibExchange") DirectExchange exchange, @Qualifier("fibQueue") Queue queue) {
			Binding b = BindingBuilder.bind(queue).to(exchange).with("rpc");
			b.setAdminsThatShouldDeclare(rabbit1Admin());
			return b;
		}

		@Bean
		public Binding squareBinding(@Qualifier("squareExchange") DirectExchange exchange, @Qualifier("squareQueue") Queue queue) {
			Binding b = BindingBuilder.bind(queue).to(exchange).with("rpc");
			b.setAdminsThatShouldDeclare(rabbit2Admin());
			return b;
		}

		@Bean
		public SimpleMessageListenerContainer fibContainer(){
			SimpleMessageListenerContainer ret = new SimpleMessageListenerContainer();
			ret.setConnectionFactory(r1cf());
			ret.setQueueNames(fibQueue().getName());
			ret.setMessageListener(new MessageListenerAdapter(server(), "fibonacci"));
			return ret;
		}

		@Bean
		public SimpleMessageListenerContainer squareContainer(){
			SimpleMessageListenerContainer ret = new SimpleMessageListenerContainer();
			ret.setConnectionFactory(r2cf());
			ret.setQueueNames(squareQueue().getName());
			ret.setMessageListener(new MessageListenerAdapter(server(), "square"));
			return ret;
		}


		@Bean
		public Tut6Server server() {
			return new Tut6Server();
		}

	}

}
