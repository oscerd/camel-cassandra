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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 *  Represents a Cassandra Consumer
 */
public class CassandraConsumer extends ScheduledPollConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraConsumer.class);
    
	/**
	 * @param endpoint
	 * @param processor
	 */
    public CassandraConsumer(CassandraEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    public CassandraEndpoint getEndpoint() {
        return (CassandraEndpoint) super.getEndpoint();
    }

    /**
    * Method that poll a specific Cassandra instance and work on his data
    * 
    */
    @Override
    protected int poll() throws Exception {
        String host = getEndpoint().getHost();
        String[] hostLists = splitHost(host);
        String port = getEndpoint().getPort();
        String keySpace = getEndpoint().getKeyspace();
        String pollingQuery = getEndpoint().getPollingQuery();
        Cluster cluster = getEndpoint().getCassandraCluster();
        Cluster.Builder builder;
        if (hostLists.length == 0) builder = Cluster.builder().addContactPoint(host).withPort(Integer.parseInt(port));
        else builder = Cluster.builder().addContactPoints(hostLists).withPort(Integer.parseInt(port));
        if (!ObjectHelper.isEmpty(getEndpoint().getUsername()) && !ObjectHelper.isEmpty(getEndpoint().getPassword())){
        	builder.withCredentials(getEndpoint().getUsername(), getEndpoint().getPassword());
        }
        cluster = builder.build();
        Session session = cluster.connect(keySpace);
        ResultSet resultSet = null;
        try {
            resultSet = session.execute(pollingQuery);
        } catch (Exception e) {
            throw new CassandraException("Error during execution of polling query: " + pollingQuery, e);
        } finally {
            session.close();
            if (!getEndpoint().isExternalCluster) cluster.close();
        }
        Exchange exchange = getEndpoint().createExchange();
        Message message = exchange.getIn();
        fillMessage(resultSet, message);
        try {
            getProcessor().process(exchange);
            return 1; 
        } finally {
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error while processing exchange", exchange, exchange.getException());
            }
        }
    }

    /**
     * Copy ResultSet into Message.
     */
    protected void fillMessage(ResultSet resultSet, Message message) {
        message.setBody(resultSet);
    }
    
    private String[] splitHost(String hostList){
    	String[] hosts = StringUtils.split(hostList, ",");
    	return hosts;
    }
}
