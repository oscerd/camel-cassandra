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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import org.apache.camel.builder.RouteBuilder;

import com.github.oscerd.component.cassandra.embedded.CassandraBaseTest;
import com.github.oscerd.component.cassandra.embedded.CassandraPlaylistBaseTest;

import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class CassandraSelectAllLimitPlaylistTest extends CassandraPlaylistBaseTest {

    @Test
    public void testSelectAllPlaylistLimit() throws IOException, InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        String body = "";
        Map<String, Object> headers = new HashMap<String, Object>();
        String addr = "127.0.0.1";
        List<String> collAddr = new ArrayList<String>();
        collAddr.add(addr);
        headers.put(CassandraConstants.CASSANDRA_CONTACT_POINTS, collAddr);
        headers.put(CassandraConstants.CASSANDRA_WHERE_COLUMN, "id");
        headers.put(CassandraConstants.CASSANDRA_WHERE_VALUE, UUID.fromString("62c36092-82a1-3a00-93d1-46196ee77204"));
        headers.put(CassandraConstants.CASSANDRA_OPERATOR, "eq");
        headers.put(CassandraConstants.CASSANDRA_ORDERBY_COLUMN, "song_order");
        headers.put(CassandraConstants.CASSANDRA_ORDER_DIRECTION, "desc");
        headers.put(CassandraConstants.CASSANDRA_LIMIT_NUMBER, 1);
        ResultSet result = (ResultSet) template.requestBodyAndHeaders("direct:in", body, headers); 
        assertEquals(1, result.getAvailableWithoutFetching());
        Iterator it = result.iterator();
        while (it.hasNext()){
        	Row row = (Row) it.next();
        	assertEquals(3, row.getInt("song_order"));
        }
        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:in")
                    .to("cassandra:cassandraConnection?keyspace=simplex&table=playlists&operation=selectAllWhere")
                    .to("mock:result");
            }
        };
    }
}
