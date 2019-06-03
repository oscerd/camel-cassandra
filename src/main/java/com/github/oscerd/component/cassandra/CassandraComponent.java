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

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the component that manages {@link CassandraEndpoint}.
 */
public class CassandraComponent extends DefaultComponent {
    
    private static final Logger LOG = LoggerFactory.getLogger(CassandraComponent.class);

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        CassandraEndpoint endpoint = new CassandraEndpoint(uri, this, remaining);
        setProperties(endpoint, parameters);
        // if its a bean reference to either a cluster/session then lookup
        if (remaining.startsWith("bean:")) {
            String beanRef = remaining.substring(5);
            endpoint.setBeanRef(beanRef);
        } 
        return endpoint;
    }
    
  
    public static CassandraException wrapInCamelCassandraException(Throwable t) {
        if (t instanceof CassandraException) {
            return (CassandraException) t;
        } else {
            return new CassandraException(t);
        }
    }
}
