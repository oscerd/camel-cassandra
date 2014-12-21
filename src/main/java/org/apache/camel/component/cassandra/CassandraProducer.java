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
import java.util.List;
import java.util.Map;

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
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

/**
 * The MongoDb producer.
 */
public class CassandraProducer extends DefaultProducer {
	private static final Logger LOG = LoggerFactory.getLogger(CassandraProducer.class);

	private CassandraEndpoint endpoint;

	public CassandraProducer(CassandraEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	public void process(Exchange exchange) throws Exception {
		Cluster cassandra = endpoint.getCassandraCluster();
		Collection<InetAddress> contact = (Collection<InetAddress>) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS);
		String cassandra_port = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_PORT);
		if (cassandra_port == null){
			cassandra = Cluster.builder().addContactPoints(contact).build();
		} else {
			cassandra = Cluster.builder().addContactPoints(contact).withPort(Integer.parseInt(cassandra_port)).build();
		}
		String body = (String) exchange.getIn().getBody();
		if (body != null){
			Session session = cassandra.connect(endpoint.getKeyspace());
			try {
				executePlainCQLQuery(exchange, body, session);
			} catch (Exception e) {
				throw CassandraComponent.wrapInCamelCassandraException(e);
			} finally {
				session.close();
				cassandra.close();
			}
		} else {
			CassandraOperations operation = endpoint.getOperation();
			Session session = cassandra.connect(endpoint.getKeyspace());
			Object header = exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATION_HEADER);
			if (header != null) {
				LOG.debug("Overriding default operation with operation specified on header: {}", header);
				try {
					if (header instanceof CassandraOperations) {
						operation = ObjectHelper.cast(CassandraOperations.class, header);
					} else {
						// evaluate as a String
						operation = CassandraOperations.valueOf(exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATION_HEADER, String.class));
					}
				} catch (Exception e) {
					throw new CassandraException("Operation specified on header is not supported. Value: " + header, e);
				}
			}
			try {
				invokeOperation(operation, exchange, session);
			} catch (Exception e) {
				throw CassandraComponent.wrapInCamelCassandraException(e);
			} finally {
				session.close();
				cassandra.close();
			}
		}
	}

	protected void executePlainCQLQuery (Exchange exchange, String query, Session session){
		ResultSet result = null;
		result = session.execute(query);
		
		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
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
		case selectAllWhere:
			doSelectWhere(exchange, CassandraOperations.selectAllWhere, session);
			break;
		case selectColumn:
			doSelectColumn(exchange, CassandraOperations.selectColumn, session);
			break;
		case selectColumnWhere:
			doSelectColumnWhere(exchange, CassandraOperations.selectColumnWhere, session);
			break;
		case insert:
			doInsert(exchange, CassandraOperations.insert, session);
			break;
		case update:
			doUpdate(exchange, CassandraOperations.update, session);
			break;
		case deleteWhere:
			doDeleteWhere(exchange, CassandraOperations.deleteWhere, session);
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

		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
	}

	protected void doSelectWhere(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Select.Where select = null;
		CassandraOperator operator = (CassandraOperator) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
		String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
		Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
		if (operation == CassandraOperations.selectAllWhere) {
				select = QueryBuilder.select().all().from(endpoint.getTable()).where();
				if (whereColumn != null && whereValue != null){
					switch (operator) {
					case eq:
						select.and(QueryBuilder.eq(whereColumn, whereValue));
						break;
					case gt:
						select.and(QueryBuilder.gt(whereColumn, whereValue));
						break;
					case gte:
						select.and(QueryBuilder.gte(whereColumn, whereValue));
						break;
					case lt:
						select.and(QueryBuilder.lt(whereColumn, whereValue));
						break;
					case lte:
						select.and(QueryBuilder.gte(whereColumn, whereValue));
						break;
					case in:
						select.and(QueryBuilder.in(whereColumn, (List<Object>)whereValue));
						break;
					default:
						break;
					}
				}
				String column = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_ORDERBY_COLUMN);
				CassandraOperator orderDirection = (CassandraOperator) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_ORDER_DIRECTION);
				appendOrderBy(select, orderDirection, column);
				result = session.execute(select);
			}

		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
	}
	
	protected void doSelectColumnWhere(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Select.Where select = null;
		CassandraOperator operator = (CassandraOperator) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
		String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
		Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
		String selectColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_SELECT_COLUMN);
		if (operation == CassandraOperations.selectColumnWhere) {
				select = QueryBuilder.select().column(selectColumn).from(endpoint.getTable()).where();
				if (whereColumn != null && whereValue != null){
					switch (operator) {
					case eq:
						select.and(QueryBuilder.eq(whereColumn, whereValue));
						break;
					case gt:
						select.and(QueryBuilder.gt(whereColumn, whereValue));
						break;
					case gte:
						select.and(QueryBuilder.gte(whereColumn, whereValue));
						break;
					case lt:
						select.and(QueryBuilder.lt(whereColumn, whereValue));
						break;
					case lte:
						select.and(QueryBuilder.gte(whereColumn, whereValue));
						break;
					case in:
						select.and(QueryBuilder.in(whereColumn, (List<Object>)whereValue));
						break;
					default:
						break;
					}
				}
				result = session.execute(select);
			}

		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
	}
	
	protected void doSelectColumn(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Select select = null;
		String selectColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_SELECT_COLUMN);
		if (operation == CassandraOperations.selectColumn) {
				select = QueryBuilder.select().column(selectColumn).from(endpoint.getTable());
				result = session.execute(select);
			}
		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
	}
	
	protected void doInsert(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Insert insert = null;
		HashMap<String, Object> insertingObject = (HashMap<String, Object>) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_INSERT_OBJECT);
		if (operation == CassandraOperations.insert) {
			insert = QueryBuilder.insertInto(endpoint.getTable());
			Iterator insertIterator = insertingObject.entrySet().iterator();
			while(insertIterator.hasNext()){
				Map.Entry element = (Map.Entry) insertIterator.next();
				insert.value((String) element.getKey(), element.getValue());
				insertIterator.remove();
			}
			result = session.execute(insert);
		}

		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
	}

	protected void doUpdate(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Update update = null;
		CassandraOperator operator = (CassandraOperator) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
		String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
		Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
		HashMap<String, Object> updatingObject = (HashMap<String, Object>) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_UPDATE_OBJECT);
		if (operation == CassandraOperations.update) {
			update = QueryBuilder.update(endpoint.getTable());
			Iterator updateIterator = updatingObject.entrySet().iterator();
			while(updateIterator.hasNext()){
				Map.Entry element = (Map.Entry) updateIterator.next();
				update.with(QueryBuilder.set((String) element.getKey(), element.getValue()));
				updateIterator.remove();
			}
			if (whereColumn != null && whereValue != null){
				switch (operator) {
				case eq:
					update.where(QueryBuilder.eq(whereColumn, whereValue));
					break;
				case gt:
					update.where(QueryBuilder.gt(whereColumn, whereValue));
					break;
				case gte:
					update.where(QueryBuilder.gte(whereColumn, whereValue));
					break;
				case lt:
					update.where(QueryBuilder.lt(whereColumn, whereValue));
					break;
				case lte:
					update.where(QueryBuilder.gte(whereColumn, whereValue));
					break;
				case in:
					update.where(QueryBuilder.in(whereColumn, (List<Object>)whereValue));
					break;
				default:
					break;
				}
			}
			result = session.execute(update);
		}

		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
	}
	
	protected void doDeleteWhere(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
		ResultSet result = null;
		Delete.Where delete = null;
		CassandraOperator operator = (CassandraOperator) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
		String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
		Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
		if (operation == CassandraOperations.deleteWhere) {
			delete = QueryBuilder.delete().all().from(endpoint.getTable()).where();
				if (whereColumn != null && whereValue != null){
					switch (operator) {
					case eq:
						delete.and(QueryBuilder.eq(whereColumn, whereValue));
						break;
					case gt:
						delete.and(QueryBuilder.gt(whereColumn, whereValue));
						break;
					case gte:
						delete.and(QueryBuilder.gte(whereColumn, whereValue));
						break;
					case lt:
						delete.and(QueryBuilder.lt(whereColumn, whereValue));
						break;
					case lte:
						delete.and(QueryBuilder.gte(whereColumn, whereValue));
						break;
					case in:
						delete.and(QueryBuilder.in(whereColumn, (List<Object>)whereValue));
						break;
					default:
						break;
					}
				}
				result = session.execute(delete);
			}

		Message responseMessage = prepareResponseMessage(exchange);
		responseMessage.setBody(result);
	}
	
	private void appendOrderBy(Select.Where select, CassandraOperator orderDirection, String columnName){
		if (columnName != null && orderDirection != null){
				if (orderDirection.equals(CassandraOperator.asc)){
					select.orderBy(QueryBuilder.asc((String) columnName));
				} else {
					select.orderBy(QueryBuilder.desc((String) columnName));
				}
			}
	}
	
	private Message prepareResponseMessage(Exchange exchange) {
		Message answer = exchange.getOut();
		MessageHelper.copyHeaders(exchange.getIn(), answer, false);
		answer.setBody(exchange.getIn().getBody());
		return answer;
	}

}
