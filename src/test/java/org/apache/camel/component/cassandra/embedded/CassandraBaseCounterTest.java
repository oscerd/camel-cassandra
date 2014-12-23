package org.apache.camel.component.cassandra.embedded;

import io.teknek.farsandra.Farsandra;
import io.teknek.farsandra.LineHandler;
import io.teknek.farsandra.ProcessHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.camel.component.cassandra.embedded.dto.Song;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Update.Where;

/**
 * Sample on how to spin up an embedded Cassandra instance
 * using in-memory storage.
 */
public class CassandraBaseCounterTest extends CamelTestSupport{

	private Farsandra fs;

	@Override
    public void doPostSetup() {

    	fs = new Farsandra();
    	fs.withVersion("2.0.3");
    	fs.withCleanInstanceOnStart(true);
    	fs.withInstanceName("target/1");
    	fs.withCreateConfigurationFiles(true);
    	fs.withHost("localhost");
    	fs.withSeeds(Arrays.asList("localhost"));
    	final CountDownLatch started = new CountDownLatch(1);
    	fs.getManager().addOutLineHandler( new LineHandler(){
    	    @Override
    	    public void handleLine(String line) {
    	      if (line.contains("Listening for thrift clients...")){
    	        started.countDown();
    	      }
    	    }
    	  }
    	);
    	fs.getManager().addProcessHandler(new ProcessHandler() {
    	    @Override
    	    public void handleTermination(int exitValue) {
    	        started.countDown();
    	    }
    	});
    	fs.start();
    	try {
			started.await();
	    	Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		
        Session session = cluster.connect();
        
		session.execute("CREATE KEYSPACE IF NOT EXISTS simplex WITH replication "
				+ "= {'class':'SimpleStrategy', 'replication_factor':3};");
		session.execute("CREATE TABLE IF NOT EXISTS simplex.counter ("
				 + "like counter, " + "id int PRIMARY KEY" + ");");
		
		session.close();
		
		session = cluster.connect("simplex");
		
		Where update = QueryBuilder.update("counter").with(QueryBuilder.incr("like", 1)).where(QueryBuilder.eq("id", 1));
		session.execute(update);
		
		session.close();
		cluster.close();
		
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

    @Override
    public void tearDown() throws Exception {
        //Shutting down everything in an orderly fashion
		fs.getManager().destroy();
		Thread.sleep(10000);
		super.tearDown();
    }
}
