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

_Example 1_:

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

_Example 2_:

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

_Example 3_:

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

_Example 4_:

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

_Example 5_:

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

_Example 6_:

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

_Example 7_:

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

_Example 8_:

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

_Example 9_:

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

_Example 10_:

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

_Example 11_:

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

_Example 12_:

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
    .to("cassandra:cassandraConnection?keyspace=simplex&table=songs&operation=batchInsert")
    .to("mock:result");

```

This route will connect to the cassandra instance running on 127.0.0.1 and port 9042, and will submit a batch Insert of 7 songs.
