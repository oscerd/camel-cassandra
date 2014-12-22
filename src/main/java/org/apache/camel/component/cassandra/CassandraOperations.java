package org.apache.camel.component.cassandra;

public enum CassandraOperations {
    selectAll,
    selectAllWhere,
    selectColumn,
    selectColumnWhere,
    update,
    insert,
    deleteColumn,
    deleteWhere,
    incrCounter,
    decrCounter
}
