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
package org.apache.camel.component.cassandra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.datastax.driver.core.ResultSet;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cassandra.embedded.CassandraBaseTest;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class CassandraInsertTest extends CassandraBaseTest {

    @Test
    public void testInsert() throws IOException, InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        String body = "";
        Map<String, Object> headers = new HashMap<String, Object>();
        String addr = "127.0.0.1";
        List<String> collAddr = new ArrayList<String>();
        collAddr.add(addr);
        headers.put(CassandraConstants.CASSANDRA_CONTACT_POINTS, collAddr);
        Set<String> tags = new HashSet<String>();
        tags.add("2003");
        tags.add("Trash");
        HashMap<String, Object> insert = new HashMap<String, Object>();
        insert.put("id", 6);
        insert.put("album", "St. Anger");
        insert.put("title", "St. Anger");
        insert.put("artist", "Metallica");
        insert.put("tags", tags);
        headers.put(CassandraConstants.CASSANDRA_INSERT_OBJECT, insert);
        ResultSet result = (ResultSet) template.requestBodyAndHeaders("direct:in", body, headers); 
        assertEquals(result.isExhausted(), true);
        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:in")
                    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=insert")
                    .to("mock:result");
            }
        };
    }
}
