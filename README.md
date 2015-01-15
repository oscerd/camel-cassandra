# Cassandra Component

# Introduction

This component use the Apache Cassandra Datastax Java driver: http://www.datastax.com/documentation/developer/java-driver/2.1/java-driver/whatsNew2.html

Apache Cassandra is an open source distributed database management system designed to handle large amounts of data across many commodity servers, providing high availability with no single point of failure. Cassandra offers robust support for clusters spanning multiple datacenters, with asynchronous masterless replication allowing low latency operations for all clients.

The camel-cassandra component integrates Camel with Cassandra allowing you to interact with Cassandra keyspace both as a producer (performing operations on the keyspace tables) and as a consumer (consuming rows from keyspace tables).

# URI format

```

cassandra:name[?options]

```

# Options

Cassandra endpoints support the following options, depending on whether they are acting like a Producer or as a Consumer.

| Option              | Default | Description                                                                         | Producer     | Consumer    |
|---------------------|---------|-------------------------------------------------------------------------------------|--------------|-------------|
| host                | null    | The hosts of the Cassandra Instance, separated by comma                             |              |      x      |
| port                | null    | The port exposing the Cassandra Instance                                            |              |      x      |
| keyspace            | null    | The keyspace to work on with the component                                          |      x       |      x      |
| table               | null    | The table to work on with the component                                             |      x       |             |
| operation           | null    | The operation to do (operations are listed in the following of this document)       |      x       |             |
| pollingQuery        | null    | The query to submit when using the component as consumer                            |              |      x      |

If you need to interact with a Cassandra instance as producer you'll need to set some headers in the message. This solution was developed to make the interested camel route more readable and to create a cluster connection and a working session each time you'll use the component as producer in the route. This approach is a good solution to take advantage of flexibility and functionalities of the Cassandra Datastax Java Driver. Let's list those Headers.

Camel-Cassandra component provides fifteen headers by which you can define a different behaviour. We will talk about plain query in the following of this documentation.

| Header                                           | Type                     | Description                                                                            |
|--------------------------------------------------|--------------------------|----------------------------------------------------------------------------------------|
| CassandraConstants.CASSANDRA_CONTACT_POINTS      | List of String             | A contact points list to connect to the different Cassandra instances                  |
| CassandraConstants.CASSANDRA_PORT                | String                   | The (same) port where the different Cassandra Instances are exposed                    |
| CassandraConstants.CASSANDRA_OPERATION_HEADER    | String                   | The operation to do on the keyspace and table of Cassandra instances                   |
| CassandraConstants.CASSANDRA_SELECT_COLUMN       | String                   | If you need to select a specific column in a query, define this header                 |
| CassandraConstants.CASSANDRA_DELETE_COLUMN       | String                   | If you need to delete on a specific column in a query, define this header              |
| CassandraConstants.CASSANDRA_WHERE_COLUMN        | String                   | If you need to specify a where clause, define the interested column in this header     |
| CassandraConstants.CASSANDRA_WHERE_VALUE         | Object                   | Define the value of the interested where column in this header                         |
| CassandraConstants.CASSANDRA_OPERATOR            | String                   | Define the operator to work with on a clause (eq, in, lt, lte etc.)                    |
| CassandraConstants.CASSANDRA_ORDERBY_COLUMN      | String                   | If you need to specify an order by clause, define the interested column in this header |
| CassandraConstants.CASSANDRA_ORDER_DIRECTION     | String                   | Define the direction of the order by column in this header (asc or desc)               |
| CassandraConstants.CASSANDRA_UPDATE_OBJECT       | HashMap of String, Object  | Define an updating object to use                                                       |
| CassandraConstants.CASSANDRA_INSERT_OBJECT       | HashMap of String, Object  | Define an inserting object to use                                                      |
| CassandraConstants.CASSANDRA_COUNTER_COLUMN      | String                   | Define the name of a counter column you need to increment or decrement                 |
| CassandraConstants.CASSANDRA_COUNTER_VALUE       | String                   | Define the incrementing or decrementing value of a counter column specified            |
| CassandraConstants.CASSANDRA_BATCH_QUERY         | String                   | A query to use in a batch operation                                                    |
| CassandraConstants.CASSANDRA_BATCH_QUERY_LIST    | List of Object[]         | The object arrays to use in the batch query                                            |

If you need to execute a complex query you can set the body of your message with the plain query and execute a plain query operation.

# Operations

Here we list the possible operation to specify in the operation parameter of the URI.

- __selectAll__: A select all operation on a table of a keyspace
- __selectAllWhere__: A select all operation with a where clause on a table of a keyspace
- __selectColumn__: A select for a specific column on a table of a keyspace
- __selectColumnWhere__: A select for a specific column with a where clause on a table of a keyspace
- __update__: An update
- __insert__: An insert
- __deleteColumnWhere__: A delete for a specific column with a where clause on a table of a keyspace
- __deleteWhere__: A delete with a where clause
- __incrCounter__: An increment of a counter
- __decrCounter__: A decrement of a counter
- __batchInsert__: A batch insert

# Operators

Here we list the possible operator to specify in CASSANDRA_OPERATOR header of a message.

- __eq__: equal
- __lt__: less than
- __lte__: less than or equal
- __gt__: greater than
- __gte__: greater than or equal
- __in__: in
- __asc__: ascending
- __desc__: descending




