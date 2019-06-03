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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.CamelContextHelper;
import org.apache.camel.support.DefaultEndpoint;
import org.apache.camel.util.ObjectHelper;

import com.datastax.driver.core.Cluster;

/**
 * Represents a Cassandra endpoint. It is responsible for creating
 * {@link CassandraProducer} instances. It accepts a number of options to
 * customise the behaviour of consumers and producers.
 */
public class CassandraEndpoint extends DefaultEndpoint {

	private Cluster cassandraCluster;
	private String beanRef;
	private String keyspace;
	private String table;
	private CassandraOperations operation;
	private String host;
	private String port;
	private String pollingQuery;
	private String username;
	private String password;
	private String format = "normalResultSet";
	private String consistencyLevel;
	
	protected boolean isExternalCluster = false;
	
	private IResultSetFormatStrategy resultSetFormatStrategy;

	public CassandraEndpoint() {
	}

	public CassandraEndpoint(String uri, CassandraComponent component, String remaining) {
		super(uri, component);
	}

	@Override
	public Producer createProducer() throws Exception {
		validateProducer();
		return new CassandraProducer(this);
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		validateConsumer();
		return new CassandraConsumer(this, processor);
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}
	
    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (cassandraCluster == null && beanRef != null) {
            Object bean = CamelContextHelper.mandatoryLookup(getCamelContext(), beanRef);
        	if (bean instanceof Cluster) {
        		cassandraCluster = (Cluster) bean;
        		isExternalCluster = true;
            } else {
                throw new IllegalArgumentException("CQL Bean type should be of type Cluster but was " + bean);    			
            }
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (isExternalCluster) cassandraCluster.close();
        super.doStop();
    }

	public Cluster getCassandraCluster() {
		return cassandraCluster;
	}

	public void setCassandraCluster(Cluster cassandraCluster) {
		this.cassandraCluster = cassandraCluster;
	}

	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPollingQuery() {
		return pollingQuery;
	}

	public void setPollingQuery(String pollingQuery) {
		this.pollingQuery = pollingQuery;
	}

	public CassandraOperations getOperation() {
		return operation;
	}

	public void setOperation(String operation) throws CassandraException {
		try {
			this.operation = CassandraOperations.valueOf(operation);
		} catch (IllegalArgumentException e) {
			throw new CassandraException("Operation not supported", e);
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBeanRef() {
		return beanRef;
	}

	public void setBeanRef(String beanRef) {
		this.beanRef = beanRef;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getConsistencyLevel() {
		return consistencyLevel;
	}

	public void setConsistencyLevel(String consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
	}

	protected boolean isExternalCluster() {
		return isExternalCluster;
	}

	public IResultSetFormatStrategy getResultSetFormatStrategy() {
		return resultSetFormatStrategy;
	}

	public void setResultSetFormatStrategy(IResultSetFormatStrategy resultSetFormatStrategy) {
		this.resultSetFormatStrategy = resultSetFormatStrategy;
	}

	private void validateProducer() throws IllegalArgumentException {
		if (ObjectHelper.isEmpty(keyspace)) {
			throw new IllegalArgumentException("The parameter keyspace must be specified");
		} else {
			if (!ObjectHelper.isEmpty(operation)) {
				if (operation == CassandraOperations.selectAll || operation == CassandraOperations.selectAllWhere || operation == CassandraOperations.selectAllWhere ||
						operation == CassandraOperations.selectColumn || operation == CassandraOperations.selectColumnWhere ||
						operation == CassandraOperations.batchOperation || operation == CassandraOperations.decrCounter || 
						operation == CassandraOperations.deleteColumnWhere || operation == CassandraOperations.incrCounter || 
						operation == CassandraOperations.insert || operation == CassandraOperations.update) {
					if(ObjectHelper.isEmpty(table)){
						throw new IllegalArgumentException("The parameter table must be specified, in case of selectAllWhere, selectColumn, selectAllWhere and selectColumnWhere operation");
					}
				}
			}
		}
	}
	
	private void validateConsumer() throws IllegalArgumentException {
		if (ObjectHelper.isEmpty(host) || ObjectHelper.isEmpty(keyspace) || ObjectHelper.isEmpty(pollingQuery)) {
			throw new IllegalArgumentException("The parameters host, keyspace and pollingQuery must be specified");
		} 
	}
}
