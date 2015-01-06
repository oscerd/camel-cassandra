# Cassandra Component

# Introduction

This component use the Apache Cassandra Datastax Java driver: http://www.datastax.com/documentation/developer/java-driver/2.1/java-driver/whatsNew2.html

Apache Cassandra is an open source distributed database management system designed to handle large amounts of data across many commodity servers, providing high availability with no single point of failure. Cassandra offers robust support for clusters spanning multiple datacenters, with asynchronous masterless replication allowing low latency operations for all clients.

The camel-cassandra component integrates Camel with Cassandra allowing you to interact with Cassandra keyspace both as a producer (performing operations on the keyspace tables) and as a consumer (consuming rows from keyspace tables).

# URI format

```

cassandra:beanName[?options]

```

# Options
