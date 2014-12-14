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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

/**
 * The MongoDb producer.
 */
public class CassandraProducer extends DefaultProducer {
	private static final Logger LOG = LoggerFactory
			.getLogger(CassandraProducer.class);

	private CassandraEndpoint endpoint;

	public CassandraProducer(CassandraEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	public void process(Exchange exchange) throws Exception {
		Cluster cassandra = endpoint.getCassandraCluster();
		Collection<InetAddress> contact = (Collection<InetAddress>) exchange.getIn().getHeader(CassandraConstants.CONTACT_POINTS);
		cassandra = Cluster.builder().addContactPoints(contact).build();
		Session session = cassandra.connect(endpoint.getKeyspace());
		CassandraOperations operation = endpoint.getOperation();
		Object header = exchange.getIn().getHeader(CassandraConstants.OPERATION_HEADER);
		if (header != null) {
			LOG.debug("Overriding default operation with operation specified on header: {}", header);
			try {
				if (header instanceof CassandraOperations) {
					operation = ObjectHelper.cast(CassandraOperations.class, header);
				} else {
					// evaluate as a String
					operation = CassandraOperations.valueOf(exchange.getIn().getHeader(CassandraConstants.OPERATION_HEADER, String.class));
				}
			} catch (Exception e) {
				throw new CassandraException("Operation specified on header is not supported. Value: " + header, e);
			}
		}
		try {
			invokeOperation(operation, exchange, session);
		} catch (Exception e) {
			throw CassandraComponent.wrapInCamelCassandraException(e);
		}
	}

	/**
	 * Entry method that selects the appropriate Cassandra operation and executes
	 * it
	 * 
	 * @param operation
	 * @param exchange
	 * @param session
	 * @throws Exception
	 */
	protected void invokeOperation(CassandraOperations operation, Exchange exchange, Session session) throws Exception {
		switch (operation) {
		case selectAll:
			doSelectAll(exchange, CassandraOperations.selectAll, session);
			break;
		case selectWhere:
			doSelectWhere(exchange, CassandraOperations.selectWhere, session);
			break;
		case update:
			doUpdate(exchange, CassandraOperations.update, session);
			break;
		default:
			return;
		}
	}

	protected void doSelectAll(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;

		if (operation == CassandraOperations.selectAll) {
			Select select = QueryBuilder.select().all().from(endpoint.getTable());
			result = session.execute(select);
		}

		Message responseMessage = prepareResponseMessage(exchange, operation);
		responseMessage.setBody(result);
	}

	protected void doSelectWhere(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Select.Where select = null;
		HashMap<CassandraComparator, HashMap<String, Object>> mapWhere = (HashMap<CassandraComparator, HashMap<String, Object>>) exchange.getIn().getHeader(CassandraConstants.WHERE_CLAUSE);
		if (operation == CassandraOperations.selectWhere) {
			Iterator it = mapWhere.entrySet().iterator();
			select = QueryBuilder.select().all().from(endpoint.getTable()).where();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				CassandraComparator whereOperator = (CassandraComparator) pairs.getKey();
				switch (whereOperator) {
				case eq:
					HashMap<String, Object> values = (HashMap<String, Object>) pairs.getValue();
					Iterator itValues = values.entrySet().iterator();
					while (itValues.hasNext()) {
						Map.Entry internalValues = (Entry) itValues.next();
						select.and(QueryBuilder.eq((String) internalValues.getKey(), internalValues.getValue()));
						itValues.remove();
					}
					break;
				default:
					break;
				}
				result = session.execute(select);
			}
		}

		Message responseMessage = prepareResponseMessage(exchange, operation);
		responseMessage.setBody(result);
	}

	protected void doUpdate(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Update update = null;
		HashMap<CassandraComparator, HashMap<String, Object>> mapWhere = (HashMap<CassandraComparator, HashMap<String, Object>>) exchange.getIn().getHeader(CassandraConstants.WHERE_CLAUSE);
		HashMap<String, Object> updatingObject = (HashMap<String, Object>) exchange.getIn().getHeader(CassandraConstants.UPDATE_OBJECT);
		if (operation == CassandraOperations.update) {
			update = QueryBuilder.update(endpoint.getTable());
			Iterator updateIterator = updatingObject.entrySet().iterator();
			while(updateIterator.hasNext()){
				Map.Entry element = (Map.Entry) updateIterator.next();
				update.with(QueryBuilder.set((String) element.getKey(), element.getValue()));
				updateIterator.remove();
			}
			Iterator whereIterator = mapWhere.entrySet().iterator();
			while (whereIterator.hasNext()) {
				Map.Entry pairs = (Map.Entry) whereIterator.next();
				CassandraComparator whereOperator = (CassandraComparator) pairs.getKey();
				switch (whereOperator) {
				case eq:
					HashMap<String, Object> values = (HashMap<String, Object>) pairs.getValue();
					Iterator itValues = values.entrySet().iterator();
					int index = 0;
					while (itValues.hasNext()) {
						Map.Entry internalValues = (Entry) itValues.next();
						update.where(QueryBuilder.eq((String) internalValues.getKey(), internalValues.getValue()));
						itValues.remove();
					}
					break;
				default:
					break;
				}
				result = session.execute(update);
			}
		}

		Message responseMessage = prepareResponseMessage(exchange, operation);
		responseMessage.setBody(result);
	}
	
	private Message prepareResponseMessage(Exchange exchange, CassandraOperations operation) {
		Message answer = exchange.getOut();
		MessageHelper.copyHeaders(exchange.getIn(), answer, false);
		answer.setBody(exchange.getIn().getBody());
		return answer;
	}

}
