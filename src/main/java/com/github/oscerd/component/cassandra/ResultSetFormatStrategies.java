package com.github.oscerd.component.cassandra;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class ResultSetFormatStrategies {

    private static final IResultSetFormatStrategy rowsList = new IResultSetFormatStrategy() {

		@Override
		public Object getResult(ResultSet resultSet) {
    		List<Row> rows = new ArrayList<Row>();
    		Iterator<Row> rowIter = resultSet.iterator();
    		while (rowIter.hasNext()) {
    			rows.add(rowIter.next());
    		}
    		return rows;
		}
    };
    
    private static final IResultSetFormatStrategy normalResultSet = new IResultSetFormatStrategy() {

		@Override
		public Object getResult(ResultSet resultSet) {
			return resultSet;
		}
    };
    
    public static IResultSetFormatStrategy rowsList() {
        return rowsList;
    }
    
    public static IResultSetFormatStrategy normalResultSet() {
        return normalResultSet;
    }
	
    public static IResultSetFormatStrategy fromName(String name) {
        if (name.equals("normalResultSet")) {
        	return ResultSetFormatStrategies.normalResultSet();
        }
        if (name.equals("rowsList")) {
            return ResultSetFormatStrategies.rowsList();
        }
		return null;
    }
}
