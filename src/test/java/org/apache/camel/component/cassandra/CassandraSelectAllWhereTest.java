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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class CassandraSelectAllWhereTest extends CamelTestSupport {

	@Test
	public void testInsert() throws UnknownHostException {
		String body = "";
		Map<String, Object> headers = new HashMap<String, Object>();
		InetAddress addr = InetAddress.getByName("127.0.0.1");
		Collection<InetAddress> collAddr = new HashSet<InetAddress>();
		collAddr.add(addr);
		headers.put(CassandraConstants.CONTACT_POINTS, collAddr);
		HashMap<String, Object> mapEqual = new HashMap<String, Object>();
		List<UUID> uuidList = new ArrayList<UUID>();
		uuidList.add(UUID.fromString("a51e426a-3bbe-4bf7-9f99-1589ebb72b35"));
		uuidList.add(UUID.fromString("657521d8-b7b4-4652-9286-9cdde0649462"));
		
		mapEqual.put("id",uuidList);
		headers.put(CassandraConstants.WHERE_COLUMN, "id");
		headers.put(CassandraConstants.WHERE_VALUE, uuidList);
		headers.put(CassandraConstants.CASSANDRA_OPERATOR, CassandraOperator.in);
		ResultSet result = (ResultSet) template.requestBodyAndHeaders(
				"direct:in", body, headers);
		System.out
				.println(String
						.format("%-50s\t%-30s\t%-20s\t%-20s\n%s",
								"id",
								"title",
								"album",
								"artist",
								"------------------------------------------------------+-------------------------------+------------------------+-----------"));
		for (Row row : result) {
			System.out.println(String.format("%-50s\t%-30s\t%-20s\t%-20s",
					row.getUUID("id"), row.getString("title"),
					row.getString("album"), row.getString("artist")));
		}
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() {
				from("direct:in")
						.to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=selectAllWhere");
			}
		};
	}
}
