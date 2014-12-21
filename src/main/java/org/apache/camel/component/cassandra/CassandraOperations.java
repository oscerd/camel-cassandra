package org.apache.camel.component.cassandra;

public enum CassandraOperations {

    // read operations
    selectAll,
    selectAllWhere,
    selectColumn,
    selectColumnWhere,
    update,
    insert,
    deleteWhere
}
