[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.oscerd/camel-cassandra/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.oscerd/camel-cassandra)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# Cassandra Component

# Introduction

This component use the Apache Cassandra Datastax Java driver: http://www.datastax.com/documentation/developer/java-driver/2.1/java-driver/whatsNew2.html

Apache Cassandra is an open source distributed database management system designed to handle large amounts of data across many commodity servers, providing high availability with no single point of failure. Cassandra offers robust support for clusters spanning multiple datacenters, with asynchronous masterless replication allowing low latency operations for all clients.

The camel-cassandra component integrates Camel with Cassandra allowing you to interact with Cassandra keyspace both as a producer (performing operations on the keyspace tables) and as a consumer (consuming rows from keyspace tables).

# Usage

Maven users will need to add the following dependency to their pom.xml for this component:

```xml
<dependency>
    <groupId>com.github.oscerd</groupId>
    <artifactId>camel-cassandra</artifactId>
    <version>2.19.1</version>
</dependency>
```

The latest release uses camel-core version 2.18.0 as dependency.

# Use this component on Apache Karaf

Camel-Cassandra component is based on camel-core 2.18.0 release. 

- Inside Karaf execute the following instructions:

```shell

karaf@root> features:repo-add mvn:com.github.oscerd/camel-cassandra/2.18.0/xml/features


```

- __Install camel-cassandra feature__

```shell

karaf@root> feature:install camel-cassandra

```

Now we are ready to deploy bundle based on camel-cassandra component. See __Code Examples__ section for a ready bundle to use.

# URI format

```

cassandra:name[?options]

```

# Options

Cassandra endpoints support the following options, depending on whether they are acting like a Producer or as a Consumer.

| Option              | Default | Description                                                                         | Producer     | Consumer    |
|---------------------|---------|-------------------------------------------------------------------------------------|--------------|-------------|
| host                | null    | The hosts of the Cassandra Instance, separated by comma                             |              |      x      |
| port                | null    | The port exposing the Cassandra Instance                                            |      x       |      x      |
| keyspace            | null    | The keyspace to work on with the component                                          |      x       |      x      |
| table               | null    | The table to work on with the component                                             |      x       |             |
| operation           | null    | The operation to do (operations are listed in the following of this document)       |      x       |             |
| pollingQuery        | null    | The query to submit when using the component as consumer                            |              |      x      |
| format              | normalResultSet    | The format of resultSet (values normalResultSet or rowsList)             |      x       |             |
| username            | null    | The username to connect to a Cassandra Cluster using Authentication/Authorization   |      x       |      x      |
| password            | null    | The password to connect to a Cassandra Cluster using Authentication/Authorization   |      x       |      x      |
| bean:clusterRef     | null    | Provided cluster reference                                                          |      x       |             |

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
| CassandraConstants.CASSANDRA_LIMIT_NUMBER        | Integer                  | Limit the number of rows returned by a query                                           |
| CassandraConstants.CASSANDRA_INDEX_NAME          | String                   | An index name                                                                          |
| CassandraConstants.CASSANDRA_INDEX_COLUMN        | String                   | A column to associate an index with                                                    |

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
- __batchOperation__: A batch operation
- __createIndex__: A create index operation
- __dropIndex__: A drop index operation

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

# Examples

- Consumer

Example of camel-cassandra used as Consumer:

```java

from("cassandra:cluster?host=127.0.0.1&port=9042&keyspace=simplex&pollingQuery=select * from songs")
    .to("mock:result");

```

This route will poll a Cassandra instances running on 127.0.0.1 on port 9042. The keyspace will be simplex and the polling query _select * from songs_ . The result will be a Datastax Java Driver result set.

- Producer

Examples of camel-cassandra used as Producer:

_Example 1_: Select All

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=selectAll")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will query all rows on the keyspace simplex and table songs.

_Example 2_: Select All with Where Clause

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("album"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant("The gathering"))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=selectAllWhere")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will query all rows on the keyspace simplex and table songs where the column _album_ is equal to "The gathering". 
Obviously we need to ensure index on the column album to make this query works.

_Example 3_: Select column

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_SELECT_COLUMN, constant("title"))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=selectColumn")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will query for title column on all the rows on the keyspace simplex and table songs.

_Example 4_: Select specific column with Where Clause

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("album"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant("The gathering"))
    .setHeader(CassandraConstants.CASSANDRA_SELECT_COLUMN, constant("title"))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=selectColumnWhere")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will query for title column on all the rows on the keyspace simplex and table songs, where the album column is equal to "The gathering". Obviously we need to ensure index on the columns _album_ and _title_ to make this query works.

_Example 5_: Insert object

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);

Set<String> tags = new HashSet<String>();
tags.add("2003");
tags.add("Trash");
HashMap<String, Object> insert = new HashMap<String, Object>();
insert.put("id", 6);
insert.put("album", "St. Anger");
insert.put("title", "St. Anger");
insert.put("artist", "Metallica");
insert.put("tags", tags);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_INSERT_OBJECT, constant(insert))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=insert")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will insert a song into the songs table of simplex keyspace.

_Example 6_: Update with Where clause

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);

HashMap<String, Object> updatingObject = new HashMap<String, Object>();
updatingObject.put("album", "Low");
updatingObject.put("title", "Low");
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("id"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant(1))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .setHeader(CassandraConstants.CASSANDRA_UPDATE_OBJECT, constant(updatingObject))
    .to("cassandra:simplex?keyspace=simplex&table=songs&operation=update")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will update the song with _id_ equal to 1 into the songs table of simplex keyspace, changing the _album_ and _title_ columns.

_Example 7_: Delete with Where clause

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("id"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant(6))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=deleteWhere")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will delete the song with _id_ equal to 6 into the songs table of simplex keyspace.

_Example 8_: Delete Column with Where clause

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("id"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant(6))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .setHeader(CassandraConstants.CASSANDRA_DELETE_COLUMN, constant("tags"))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=deleteColumnWhere")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will delete the column _tags_ of the song with _id_ equal to 6 into the songs table of simplex keyspace.

_Example 9_: Increment a counter column

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("id"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant(1))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .setHeader(CassandraConstants.CASSANDRA_COUNTER_COLUMN, constant("like"))
    .setHeader(CassandraConstants.CASSANDRA_COUNTER_VALUE, constant(new Long(5)))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=counter&operation=incrCounter")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will increment of 5 units the _like_ counter of the song with _id_ equal to 1, into the songs table of simplex keyspace.

_Example 10_: Decrement a counter column

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("id"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant(1))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .setHeader(CassandraConstants.CASSANDRA_COUNTER_COLUMN, constant("like"))
    .setHeader(CassandraConstants.CASSANDRA_COUNTER_VALUE, constant(new Long(5)))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=counter&operation=decrCounter")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will decrement of 5 units the _like_ counter of the song with _id_ equal to 1, into the songs table of simplex keyspace.

_Example 11_: Plain query 

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setBody(constant("SELECT id, album, title FROM songs"))
    .to("cassandra:cassandraConnection?keyspace=simplex")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will submit the plain query _SELECT id, album, title FROM songs_

_Example 12_: Batch Insert

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);

List<Object[]> objectArrayList = new ArrayList<Object[]>();
Object[] object = {7, "Fight Fire with Fire", "Ride the Lightning", "Metallica"};
Object[] object1 = {8, "Ride the Lightning", "Ride the Lightning", "Metallica"};
Object[] object2 = {9, "For Whom the Bell Tolls", "Ride the Lightning", "Metallica"};
Object[] object3 = {10, "Fade To Black", "Ride the Lightning", "Metallica"};
Object[] object4 = {11, "Trapped Under Ice", "Ride the Lightning", "Metallica"};
Object[] object5 = {12, "Escape", "Ride the Lightning", "Metallica"};
Object[] object6 = {13, "Creeping Death", "Ride the Lightning", "Metallica"};
Object[] object7 = {14, "The Call of Ktulu", "Ride the Lightning", "Metallica"};
objectArrayList.add(object);
objectArrayList.add(object1);
objectArrayList.add(object2);
objectArrayList.add(object3);
objectArrayList.add(object4);
objectArrayList.add(object5);
objectArrayList.add(object6);
objectArrayList.add(object7);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY, constant("INSERT INTO songs (id, title, album, artist) VALUES (?, ?, ?, ?);"))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, constant(objectArrayList))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchOperation")
    .to("mock:result");

```

_Example 13_: Select with Where Clause, Order by Clause and Limit clause

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_COLUMN, constant("id"))
    .setHeader(CassandraConstants.CASSANDRA_WHERE_VALUE, constant(UUID.fromString("62c36092-82a1-3a00-93d1-46196ee77204")))
    .setHeader(CassandraConstants.CASSANDRA_OPERATOR, constant("eq"))
    .setHeader(CassandraConstants.CASSANDRA_ORDERBY_COLUMN, constant("song_order"))
    .setHeader(CassandraConstants.CASSANDRA_ORDER_DIRECTION, constant("desc"))
    .setHeader(CassandraConstants.CASSANDRA_LIMIT_NUMBER, constant(2))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchInsert")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will query for a specific song with id 62c36092-82a1-3a00-93d1-46196ee77204, order the result for column "song_order" and limit the result to 2 rows.

_Example 14_: Using a Cluster reference

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);

Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
JndiRegistry reg = getContext().getRegistry();
registry.bind("cassandraConnection", cluster);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .to("cassandra:bean:cassandraConnection?keyspace=simplex&table=songs&operation=selectAll")
    .to("mock:result");

```

_Example 15_: Multiple Batch Operation

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);

final List<Object[]> objectArrayListInsert = new ArrayList<Object[]>();
Object[] object = {7, "Fight Fire with Fire", "Ride the Lightning", "Metallica"};
Object[] object1 = {8, "Ride the Lightning", "Ride the Lightning", "Metallica"};
Object[] object2 = {9, "For Whom the Bell Tolls", "Ride the Lightning", "Metallica"};
Object[] object3 = {10, "Fade To Black", "Ride the Lightning", "Metallica"};
Object[] object4 = {11, "Trapped Under Ice", "Ride the Lightning", "Metallica"};
Object[] object5 = {12, "Escape", "Ride the Lightning", "Metallica"};
Object[] object6 = {13, "Creeping Death", "Ride the Lightning", "Metallica"};
Object[] object7 = {14, "The Call of Ktulu", "Ride the Lightning", "Metallica"};
objectArrayListInsert.add(object);
objectArrayListInsert.add(object1);
objectArrayListInsert.add(object2);
objectArrayListInsert.add(object3);
objectArrayListInsert.add(object4);
objectArrayListInsert.add(object5);
objectArrayListInsert.add(object6);
objectArrayListInsert.add(object7);
        
final String insertBatch = "INSERT INTO songs (id, title, album, artist) VALUES (?, ?, ?, ?);";
        
final List<Object[]> objectArrayListUpdate = new ArrayList<Object[]>();
Object[] object8 = {"Wings for Marie", "10000 days", "Tool", 1};
Object[] object9 = {"10000 days", "10000 days", "Tool", 2};
Object[] object10 = {"Vicarious", "10000 days", "Tool", 3};
objectArrayListUpdate.add(object8);
objectArrayListUpdate.add(object9);
objectArrayListUpdate.add(object10);
        
final String updateBatch = "UPDATE songs SET title = ?, album = ?, artist = ? where id = ?;";
        
final List<Object[]> objectArrayListDelete = new ArrayList<Object[]>();
Object[] object11 = {1};
objectArrayListDelete.add(object11);
        
final String deleteBatch =  "DELETE FROM songs where id = ?";
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY, constant(insertBatch))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, constant(objectArrayListInsert))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchOperation")
    .setBody(constant(""))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY, constant(updateBatch))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, constant(objectArrayListUpdate))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchOperation")
    .setBody(constant(""))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY, constant(deleteBatch))
    .setHeader(CassandraConstants.CASSANDRA_BATCH_QUERY_LIST, constant(objectArrayListDelete))
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchOperation")
    .to("mock:result");

```

_Example 16_: Creating an index

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);

Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
JndiRegistry reg = getContext().getRegistry();
registry.bind("cassandraConnection", cluster);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_INDEX_COLUMN, constant("artist"))
    .setHeader(CassandraConstants.CASSANDRA_INDEX_NAME, constant("artist_idx"))
    .to("cassandra:bean:cassandraConnection?keyspace=simplex&table=songs&operation=createIndex")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will create an index named artist_idx on the column artist on table songs of keyspace simplex

_Example 17_: Dropping an index

```java

String addr = "127.0.0.1";
List<String> collAddr = new ArrayList<String>();
collAddr.add(addr);

Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
JndiRegistry reg = getContext().getRegistry();
registry.bind("cassandraConnection", cluster);
    
from("direct:in")
    .setHeader(CassandraConstants.CASSANDRA_CONTACT_POINTS, constant(collAddr))
    .setHeader(CassandraConstants.CASSANDRA_INDEX_NAME, constant("artist_idx"))
    .to("cassandra:bean:cassandraConnection?keyspace=simplex&table=songs&operation=dropIndex")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will drop an index named artist_idx on table songs of keyspace simplex

# Code Examples

- https://github.com/oscerd/camel-cassandra-example: A simple Camel Route using Camel-cassandra component
- https://github.com/oscerd/camel-cassandra-servicemix-example: A simple Camel Route, to be deployed on ServiceMix, using Camel-cassandra component.
- https://github.com/oscerd/camel-cassandra-servicemix-airport-example: A Camel Route that read from a csv a list of airport (46240 airports) and insert them in a Cassandra keyspace.

# ToDo List

- Add support for username/password authentication to cassandra producer [x]
- Add support for username/password authentication to cassandra consumer [x]
- Add limit parameter as header [x]
- Add support for Cluster bean reference in Cassandra Producer [x]
- Improve testing [x]
- Define a ResultSet transform parameter in Cassandra Producer [x]
- Add createIndex/dropIndex operation [x]
