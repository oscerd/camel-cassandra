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

import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import com.github.oscerd.component.cassandra.embedded.CassandraBaseTest;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class CassandraConsumerTest extends CassandraBaseTest {

    private static final String POLLING_QUERY = "select * from songs";

    @Test
    public void testConsume() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                ResultSet body = (ResultSet) exchange.getIn().getBody();
                assertEquals(body.getAvailableWithoutFetching(), 6);                
            }
        });
        mock.await(1, TimeUnit.SECONDS);
        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("cassandra:cluster?host=127.0.0.1&port=9042&keyspace=simplex&pollingQuery=" + POLLING_QUERY)
                    .to("mock:result");
            }
        };
    }
}
