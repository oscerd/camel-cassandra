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
| host                | null    | The host of the Cassandra Instance                                                  |              |      x      |
| port                | null    | The port exposing the Cassandra Instance                                            |              |      x      |
| keyspace            | null    | The keyspace to work on with the component                                          |      x       |      x      |
| table               | null    | The table to work on with the component                                             |      x       |             |
| operation           | null    | The operation to do (operations are listed in the following of this document)       |      x       |             |
| pollingQuery        | null    | The query to submit when using the component as consumer                            |              |      x      |
