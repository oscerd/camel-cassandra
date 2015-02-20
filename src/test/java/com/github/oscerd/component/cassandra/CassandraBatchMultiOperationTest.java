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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;

import org.apache.camel.builder.RouteBuilder;

import com.github.oscerd.component.cassandra.embedded.CassandraBaseTest;

import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class CassandraBatchMultiOperationTest extends CassandraBaseTest {

    @Test
    public void testBatchMultioperation() throws IOException, InterruptedException {
        List<String> existingtitleList = new ArrayList<String>();
        existingtitleList.add("10000 days");
        existingtitleList.add("Vicarious");
        
        List<String> notExistingtitleList = new ArrayList<String>();
        notExistingtitleList.add("Wings for Marie");
        
    	MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        String body = "";
        Map<String, Object> headers = new HashMap<String, Object>();
        String addr = "127.0.0.1";
        List<String> collAddr = new ArrayList<String>();
        collAddr.add(addr);
        headers.put(CassandraConstants.CASSANDRA_CONTACT_POINTS, collAddr);
        ResultSet result = (ResultSet) template.requestBodyAndHeaders("direct:in", body, headers); 
        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        Session session = cluster.connect("simplex");
        Select select = QueryBuilder.select().all().from("songs");
        result = session.execute(select);
        assertEquals(result.getAvailableWithoutFetching(), 13);
        Select.Where selectWh = QueryBuilder.select().all().from("songs").where(QueryBuilder.eq("album", "10000 days"));
        result = session.execute(selectWh);
        assertEquals(result.getAvailableWithoutFetching(), 2);
        for (Row row : (ResultSet) result) {
            assertTrue(existingtitleList.contains(row.getString("title")));
            assertFalse(notExistingtitleList.contains(row.getString("title")));
        }
        Select.Where selectWhere = QueryBuilder.select().all().from("songs").where(QueryBuilder.eq("id", 1));
        result = session.execute(selectWhere);
        assertEquals(result.getAvailableWithoutFetching(), 0);
        session.close();
        cluster.close();
        assertMockEndpointsSatisfied();
    }
    
    private List<Object[]> populateBatch() {
        List<Object[]> objectArrayList = new ArrayList<Object[]>();
        Object[] object = {1};
        objectArrayList.add(object);
        return objectArrayList;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        final List<Object[]> objectArrayListInsert = new ArrayList<Object[]>();
        Object[] object = {7, "Fight Fire with Fire", "Ride the Lightning", "Metallica"};
        Object[] object1 = {8, "Ride the Lightning", "Ride the Lightning", "Metallica"};
        Object[] object2 = {9, "For Whom the Bell Tolls", "Ride the Lightning", "Metallica"};
        Object[] object3 = {10, "Fade To Black", "Ride the Lightning", "Metallica"};
        Object[] object4 = {11, "Trapped Under Ice", "Ride the Lightning", "Metallica"};
        Object[] object5 = {12, "Escape", "Ride the Lightning", "Metallica"};
        Object[] object6 = {13, "Creeping Death", "Ride the Lightning", "Metallica"};
        Object[] object7 = {14, "The Call of Ktulu", "Ride the Lightning", "Metallica"};
        objectArrayListInsert.add(object);
        objectArrayListInsert.add(object1);
        objectArrayListInsert.add(object2);
        objectArrayListInsert.add(object3);
        objectArrayListInsert.add(object4);
        objectArrayListInsert.add(object5);
        objectArrayListInsert.add(object6);
        objectArrayListInsert.add(object7);
        
        final String insertBatch = "INSERT INTO songs (id, title, album, artist) VALUES (?, ?, ?, ?);";
        
        final List<Object[]> objectArrayListUpdate = new ArrayList<Object[]>();
        Object[] object8 = {"Wings for Marie", "10000 days", "Tool", 1};
        Object[] object9 = {"10000 days", "10000 days", "Tool", 2};
        Object[] object10 = {"Vicarious", "10000 days", "Tool", 3};
        objectArrayListUpdate.add(object8);
        objectArrayListUpdate.add(object9);
        objectArrayListUpdate.add(object10);
        
        final String updateBatch = "UPDATE songs SET title = ?, album = ?, artist = ? where id = ?;";
        
        final List<Object[]> objectArrayListDelete = new ArrayList<Object[]>();
        Object[] object11 = {1};
        objectArrayListDelete.add(object11);
        
        final String deleteBatch =  "DELETE FROM songs where id = ?";
        
        return new RouteBuilder() {
            
            public void configure() {
                from("direct:in")
                	.setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY, constant(insertBatch))
                	.setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, constant(objectArrayListInsert))
                    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchOperation")
                    .setBody(constant(""))
                	.setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY, constant(updateBatch))
                	.setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, constant(objectArrayListUpdate))
                	.to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchOperation")
                    .setBody(constant(""))
                	.setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY, constant(deleteBatch))
                	.setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, constant(objectArrayListDelete))
                	.to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchOperation")
                    .to("mock:result");
            }
        };
    }
}
