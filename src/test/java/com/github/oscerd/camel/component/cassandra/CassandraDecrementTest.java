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
package com.github.oscerd.camel.component.cassandra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

import org.apache.camel.builder.RouteBuilder;
import com.github.oscerd.camel.component.cassandra.embedded.CassandraBaseCounterTest;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class CassandraDecrementTest extends CassandraBaseCounterTest {

    @Test
    public void testDecrementCounter() throws IOException, InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        String body = "";
        Map<String, Object> headers = new HashMap<String, Object>();
        String addr = "127.0.0.1";
        List<String> collAddr = new ArrayList<String>();
        collAddr.add(addr);
        headers.put(CassandraConstants.CASSANDRA_CONTACT_POINTS, collAddr);
        headers.put(CassandraConstants.CASSANDRA_COUNTER_COLUMN, "like");
        headers.put(CassandraConstants.CASSANDRA_COUNTER_VALUE, new Long(5));
        headers.put(CassandraConstants.CASSANDRA_WHERE_COLUMN, "id");
        headers.put(CassandraConstants.CASSANDRA_WHERE_VALUE, 1);
        headers.put(CassandraConstants.CASSANDRA_OPERATOR, "eq");
        ResultSet result = (ResultSet) template.requestBodyAndHeaders("direct:in", body , headers); 
        assertEquals(result.isExhausted(), true);
        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        Session session = cluster.connect("simplex");
        Select.Where select = QueryBuilder.select().all().from("counter").where(QueryBuilder.eq("id", 1));
        result = session.execute(select);
        session.close();
        cluster.close();
        for (Row row : (ResultSet) result) {
            assertEquals(row.getLong("like"), -4);
        }
        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:in")
                    .to("cassandra:cassandraConnection?keyspace=simplex&table=counter&operation=decrCounter")
                    .to("mock:result");
            }
        };
    }
}
