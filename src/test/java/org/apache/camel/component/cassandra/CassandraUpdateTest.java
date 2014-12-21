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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cassandra.embedded.CassandraBaseTest;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class CassandraUpdateTest extends CassandraBaseTest {

	@Test
	public void testUpdate() throws IOException, InterruptedException {
		CassandraBaseTest baseTest = new CassandraBaseTest();
		baseTest.setup();
		String body = "";
		Map<String, Object> headers = new HashMap<String, Object>();
		InetAddress addr = InetAddress.getByName("127.0.0.1");
		Collection<InetAddress> collAddr = new HashSet<InetAddress>();
		collAddr.add(addr);
		headers.put(CassandraConstants.CONTACT_POINTS, collAddr);
		HashMap<String, Object> updatingObject = new HashMap<String, Object>();
		updatingObject.put("album", "Demonic");
		updatingObject.put("title", "Demonic Refusal");
		headers.put(CassandraConstants.WHERE_COLUMN, "id");
		headers.put(CassandraConstants.WHERE_VALUE, 1);
		headers.put(CassandraConstants.CASSANDRA_OPERATOR, CassandraOperator.eq);
		headers.put(CassandraConstants.UPDATE_OBJECT, updatingObject);
		ResultSet result = (ResultSet) template.requestBodyAndHeaders("direct:in", body, headers);
		assertEquals(result.wasApplied(), true);
		baseTest.shutdown();
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() {
				from("direct:in")
						.to("cassandra:simplex?keyspace=simplex&table=songs&operation=update");
			}
		};
	}
}
