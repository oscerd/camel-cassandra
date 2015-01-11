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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Update.Assignments;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Cassandra producer.
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
        List<String> contact = (List<String>) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS);
        Collection<InetAddress> contactPoints = getInetAddress(contact);
        String cassandraPort = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_PORT);
        if (cassandraPort == null) {
            cassandra = Cluster.builder().addContactPoints(contactPoints).build();
        } else {
            cassandra = Cluster.builder().addContactPoints(contactPoints).withPort(Integer.parseInt(cassandraPort)).build();
        }
        String body = (String) exchange.getIn().getBody();
        if (body != null && !ObjectHelper.isEmpty(body)) {
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

    protected void executePlainCQLQuery(Exchange exchange, String query, Session session) {
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
        case deleteColumnWhere:
            doDeleteColumnWhere(exchange, CassandraOperations.deleteColumnWhere, session);
            break;
        case deleteWhere:
            doDeleteWhere(exchange, CassandraOperations.deleteWhere, session);
            break;
        case incrCounter:
            doIncrCounter(exchange, CassandraOperations.incrCounter, session);
            break;
        case decrCounter:
            doDecrCounter(exchange, CassandraOperations.decrCounter, session);
            break;
        case batchInsert:
            doBatchInsert(exchange, CassandraOperations.batchInsert, session);
            break;
        default:
            throw new CassandraException("Operation not supported. Value: " + operation);
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
        String cassOperator = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
        CassandraOperator operator = getCassandraOperator(cassOperator);
        String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
        Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
        if (operation == CassandraOperations.selectAllWhere) {
            select = QueryBuilder.select().all().from(endpoint.getTable()).where();
            if (whereColumn != null && whereValue != null) {
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
            String cassOrderDirection = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
            CassandraOperator orderDirection = getCassandraOperator(cassOrderDirection);
            appendOrderBy(select, orderDirection, column);
            result = session.execute(select);
        }
        Message responseMessage = prepareResponseMessage(exchange);
        responseMessage.setBody(result);
    }

    protected void doSelectColumnWhere(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
        ResultSet result = null;
        Select.Where select = null;
        String cassOperator = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
        CassandraOperator operator = getCassandraOperator(cassOperator);
        String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
        Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
        String selectColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_SELECT_COLUMN);
        if (operation == CassandraOperations.selectColumnWhere) {
            select = QueryBuilder.select().column(selectColumn).from(endpoint.getTable()).where();
            if (whereColumn != null && whereValue != null) {
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
            while (insertIterator.hasNext()) {
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
        String cassOperator = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
        CassandraOperator operator = getCassandraOperator(cassOperator);
        String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
        Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
        HashMap<String, Object> updatingObject = (HashMap<String, Object>) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_UPDATE_OBJECT);
        if (operation == CassandraOperations.update) {
            update = QueryBuilder.update(endpoint.getTable());
            Iterator updateIterator = updatingObject.entrySet().iterator();
            while (updateIterator.hasNext()) {
                Map.Entry element = (Map.Entry) updateIterator.next();
                update.with(QueryBuilder.set((String) element.getKey(), element.getValue()));
                updateIterator.remove();
            }
            if (whereColumn != null && whereValue != null) {
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
        String cassOperator = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
        CassandraOperator operator = getCassandraOperator(cassOperator);
        String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
        Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
        if (operation == CassandraOperations.deleteWhere) {
            delete = QueryBuilder.delete().all().from(endpoint.getTable()).where();
            if (whereColumn != null && whereValue != null) {
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

    protected void doDeleteColumnWhere(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
        ResultSet result = null;
        Delete.Where delete = null;
        String deleteColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_DELETE_COLUMN);
        String cassOperator = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
        CassandraOperator operator = getCassandraOperator(cassOperator);
        String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
        Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
        if (operation == CassandraOperations.deleteColumnWhere) {
            delete = QueryBuilder.delete().column(deleteColumn).from(endpoint.getTable()).where();
            if (whereColumn != null && whereValue != null) {
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

    protected void doIncrCounter(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
        ResultSet result = null;
        Assignments update = null;
        String counterColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_COUNTER_COLUMN);
        long counterValue = (long) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_COUNTER_VALUE);
        String cassOperator = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
        CassandraOperator operator = getCassandraOperator(cassOperator);
        String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
        Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
        if (operation == CassandraOperations.incrCounter) {
            update = QueryBuilder.update(endpoint.getTable()).with(QueryBuilder.incr(counterColumn, counterValue));
            if (whereColumn != null && whereValue != null) {
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

    protected void doDecrCounter(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
        ResultSet result = null;
        Assignments update = null;
        String counterColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_COUNTER_COLUMN);
        long counterValue = (long) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_COUNTER_VALUE);
        String cassOperator = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_OPERATOR);
        CassandraOperator operator = getCassandraOperator(cassOperator);
        String whereColumn = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN);
        Object whereValue = (Object) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_WHERE_VALUE);
        if (operation == CassandraOperations.decrCounter) {
            update = QueryBuilder.update(endpoint.getTable()).with(QueryBuilder.decr(counterColumn, counterValue));
            if (whereColumn != null && whereValue != null) {
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
    
    protected void doBatchInsert(Exchange exchange, CassandraOperations operation, Session session) throws Exception {
        ResultSet result = null;
        PreparedStatement preparedStatement = null;
        String batchQuery = (String) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_BATCH_QUERY);
        List<Object[]> objectArrayList = (List<Object[]>) exchange.getIn().getHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST);
        if (operation == CassandraOperations.batchInsert) {
            if (batchQuery != null && objectArrayList != null) {
                preparedStatement = session.prepare(batchQuery);
                BatchStatement batch = new BatchStatement();
                Iterator objectArrayIterator = objectArrayList.iterator();
                while (objectArrayIterator.hasNext()) {
                    Object[] objectArray = (Object[]) objectArrayIterator.next();
                    batch.add(preparedStatement.bind(objectArray));
                    objectArrayIterator.remove();
                }
                result = session.execute(batch);
            }
        }
        Message responseMessage = prepareResponseMessage(exchange);
        responseMessage.setBody(result);
    }

    private void appendOrderBy(Select.Where select, CassandraOperator orderDirection, String columnName) {
        if (columnName != null && orderDirection != null) {
            if (orderDirection.equals(CassandraOperator.asc)) {
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

    private Collection<InetAddress> getInetAddress(List<String> addr) throws UnknownHostException {
        Collection<InetAddress> coll = new HashSet<InetAddress>();
        Iterator it = addr.iterator();
        while (it.hasNext()) {
            String address = (String) it.next();
            coll.add(InetAddress.getByName(address));
        }
        return coll;
    }

    private CassandraOperator getCassandraOperator(String operator) throws UnknownHostException {
        CassandraOperator cassOperator = null;
        switch (operator) {
        case "eq":
            cassOperator = CassandraOperator.eq;
            break;
        case "gt":
            cassOperator = CassandraOperator.gt;
            break;
        case "gte":
            cassOperator = CassandraOperator.gte;
            break;
        case "lt":
            cassOperator = CassandraOperator.lt;
            break;
        case "lte":
            cassOperator = CassandraOperator.lte;
            break;
        case "in":
            cassOperator = CassandraOperator.in;
            break;
        case "asc":
            cassOperator = CassandraOperator.asc;
            break;
        case "desc":
            cassOperator = CassandraOperator.desc;
            break;
        default:
            break;
        }
        return cassOperator;
    }
}
