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

/**
 * Represents the constants used by {@link CassandraComponent}.
 */
public final class CassandraConstants {
    public static final String CASSANDRA_CONTACT_POINTS = "CamelCassandraContactPoints";
    public static final String CASSANDRA_PORT = "CamelCassandraPort";
    public static final String CASSANDRA_OPERATION_HEADER = "CamelCassandraDbOperation";
    public static final String CASSANDRA_SELECT_COLUMN = "CamelCassandraSelectColumn";
    public static final String CASSANDRA_DELETE_COLUMN = "CamelCassandraDeleteColumn";
    public static final String CASSANDRA_WHERE_COLUMN = "CamelCassandraWhereClause";
    public static final String CASSANDRA_WHERE_VALUE = "CamelCassandraWhereValue";
    public static final String CASSANDRA_OPERATOR = "CamelCassandraOperator";
    public static final String CASSANDRA_ORDERBY_COLUMN = "CamelCassandraOrderByColumn";
    public static final String CASSANDRA_ORDER_DIRECTION = "CamelCassandraOrderDirection";
    public static final String CASSANDRA_UPDATE_OBJECT = "CamelCassandraUpdateObject";
    public static final String CASSANDRA_INSERT_OBJECT = "CamelCassandraInsertObject";
    public static final String CASSANDRA_COUNTER_COLUMN = "CamelCassandraCounterColumn";
    public static final String CASSANDRA_COUNTER_VALUE = "CamelCassandraCounterValue";
    public static final String CASSANDRA_BATCH_QUERY = "CamelCassandraBatchQuery";
    public static final String CASSANDRA_BATCH_QUERY_LIST = "CamelCassandraBatchQueryList";
    public static final String CASSANDRA_LIMIT_NUMBER = "CamelCassandraLimitNumber";
    public static final String CASSANDRA_INDEX_COLUMN = "CamelCassandraIndexColumn";
    public static final String CASSANDRA_INDEX_NAME = "CamelCassandraIndexName";
   
    private CassandraConstants() {
    }

}
