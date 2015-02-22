/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.oscerd.component.cassandra;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.junit.Test;

public class CassandraValidationTest{

    @Test(expected=IllegalArgumentException.class)
    public void testConsumerValidationKo() throws Exception {
    	CassandraEndpoint endpoint = new CassandraEndpoint();
    	endpoint.setPort("9042");
    	Processor p = new Processor() {			
			@Override
			public void process(Exchange arg0) throws Exception {				
			}
		};
    	endpoint.createConsumer(p);
    }
    
    @Test
    public void testConsumerValidationOk() throws Exception {
    	CassandraEndpoint endpoint = new CassandraEndpoint();
    	endpoint.setPort("9042");
    	endpoint.setKeyspace("simplex");
    	endpoint.setHost("127.0.0.1");
    	endpoint.setPollingQuery("select * from songs");
    	Processor p = new Processor() {			
			@Override
			public void process(Exchange arg0) throws Exception {				
			}
		};
    	endpoint.createConsumer(p);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testProducerValidationKoNoKeyspace() throws Exception {
    	CassandraEndpoint endpoint = new CassandraEndpoint();
    	endpoint.setOperation("selectAll");
    	endpoint.createProducer();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testProducerValidationKoNoTable() throws Exception {
    	CassandraEndpoint endpoint = new CassandraEndpoint();
    	endpoint.setOperation("selectAll");
    	endpoint.setKeyspace("simplex");
    	endpoint.createProducer();
    }
    
    @Test
    public void testProducerValidationOk() throws Exception {
    	CassandraEndpoint endpoint = new CassandraEndpoint();
    	endpoint.setOperation("selectAll");
    	endpoint.setKeyspace("simplex");
    	endpoint.setTable("songs");
    	endpoint.createProducer();
    }
}
