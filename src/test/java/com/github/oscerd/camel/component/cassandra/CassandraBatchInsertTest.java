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
import com.datastax.driver.core.querybuilder.Select.Where;

import org.apache.camel.builder.RouteBuilder;
import com.github.oscerd.camel.component.cassandra.embedded.CassandraBaseTest;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class CassandraBatchInsertTest extends CassandraBaseTest {

    @Test
    public void testBatchInsert() throws IOException, InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        String body = "";
        Map<String, Object> headers = new HashMap<String, Object>();
        String addr = "127.0.0.1";
        List<String> collAddr = new ArrayList<String>();
        collAddr.add(addr);
        headers.put(CassandraConstants.CASSANDRA_CONTACT_POINTS, collAddr);
        List<Object[]> listArray = new ArrayList<Object[]>();
        listArray = populateBatch();
        headers.put(CassandraConstants.CASSANDRA_BATCH_QUERY, "INSERT INTO songs (id, title, album, artist) VALUES (?, ?, ?, ?);");
        headers.put(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, listArray);
        ResultSet result = (ResultSet) template.requestBodyAndHeaders("direct:in", body, headers); 
        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        Session session = cluster.connect("simplex");
        Where select = QueryBuilder.select().all().from("songs").where(QueryBuilder.eq("album", "Ride the Lightning"));
        result = session.execute(select);
        session.close();
        cluster.close();
        assertEquals(result.getAvailableWithoutFetching(), 8);
        for (Row row : (ResultSet) result) {
            assertEquals(row.getString("artist"), "Metallica");
        }
        assertMockEndpointsSatisfied();
    }
    
    private List<Object[]> populateBatch() {
        List<Object[]> objectArrayList = new ArrayList<Object[]>();
        Object[] object = {7, "Fight Fire with Fire", "Ride the Lightning", "Metallica"};
        Object[] object1 = {8, "Ride the Lightning", "Ride the Lightning", "Metallica"};
        Object[] object2 = {9, "For Whom the Bell Tolls", "Ride the Lightning", "Metallica"};
        Object[] object3 = {10, "Fade To Black", "Ride the Lightning", "Metallica"};
        Object[] object4 = {11, "Trapped Under Ice", "Ride the Lightning", "Metallica"};
        Object[] object5 = {12, "Escape", "Ride the Lightning", "Metallica"};
        Object[] object6 = {13, "Creeping Death", "Ride the Lightning", "Metallica"};
        Object[] object7 = {14, "The Call of Ktulu", "Ride the Lightning", "Metallica"};
        objectArrayList.add(object);
        objectArrayList.add(object1);
        objectArrayList.add(object2);
        objectArrayList.add(object3);
        objectArrayList.add(object4);
        objectArrayList.add(object5);
        objectArrayList.add(object6);
        objectArrayList.add(object7);
        return objectArrayList;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:in")
                    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchInsert")
                    .to("mock:result");
            }
        };
    }
}
