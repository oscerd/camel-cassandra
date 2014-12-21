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
import org.junit.Before;
import org.junit.BeforeClass;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 * Sample on how to spin up an embedded Cassandra instance
 * using in-memory storage.
 */
public class CassandraBaseTest extends CamelTestSupport{

	private Farsandra fs;

	@Before
    public void setup() throws IOException, InterruptedException {

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
    	        System.out.println("Cassandra terminated with exit value: " + exitValue);
    	        started.countDown();
    	    }
    	});
    	fs.start();
    	started.await();
    	Thread.sleep(10000);

        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		
        Session session = cluster.connect();
        
		session.execute("CREATE KEYSPACE IF NOT EXISTS simplex WITH replication "
				+ "= {'class':'SimpleStrategy', 'replication_factor':3};");
		session.execute("CREATE TABLE IF NOT EXISTS simplex.songs ("
				+ "id int PRIMARY KEY," + "title text," + "album text,"
				+ "artist text," + "tags set<text>," + "data blob" + ");");
		session.execute("CREATE INDEX album_idx ON simplex.songs(album);");
		session.execute("CREATE INDEX title_idx ON simplex.songs(title);");
		
		PreparedStatement statement = session
				.prepare("INSERT INTO simplex.songs "
						+ "(id, title, album, artist, tags) "
						+ "VALUES (?, ?, ?, ?, ?);");

		BoundStatement boundStatement = new BoundStatement(statement);
		
		List<Song> songList = new ArrayList<Song>();
		
		prepareStartingData(songList);
		
		Iterator it = songList.iterator();
		
		while (it.hasNext()){
			Song song = (Song) it.next();
			ResultSet res = session.execute(boundStatement.bind(
					song.getId(),
					song.getTitle(), song.getAlbum(),
					song.getArtist(), song.getTags()));
		}
		
		session.close();
		cluster.close();
		
		Thread.sleep(5 * 1000);

    }
    
    private void prepareStartingData(List<Song> songList){
    	Song song = new Song();
    	song.setId(1);
    	song.setArtist("Testament");
    	song.setTitle("DNR");
    	song.setAlbum("The gathering");
		Set<String> tags = new HashSet<String>();
		tags.add("metal");
		tags.add("1999");
		song.setTags(tags);
		
		songList.add(song);
		
		song = new Song();
    	song.setId(2);
    	song.setArtist("Testament");
    	song.setTitle("Down For Life");
    	song.setAlbum("The gathering");
		tags = new HashSet<String>();
		tags.add("metal");
		tags.add("1999");
		song.setTags(tags);
		
		songList.add(song);
		
		song = new Song();
    	song.setId(3);
    	song.setArtist("Testament");
    	song.setTitle("Riding The Snake");
    	song.setAlbum("The gathering");
		tags = new HashSet<String>();
		tags.add("metal");
		tags.add("1999");
		song.setTags(tags);
		
		songList.add(song);
		
		song = new Song();
    	song.setId(4);
    	song.setArtist("Testament");
    	song.setTitle("True Believer");
    	song.setAlbum("The gathering");
		tags = new HashSet<String>();
		tags.add("metal");
		tags.add("1999");
		song.setTags(tags);
		
		songList.add(song);
		
		song = new Song();
    	song.setId(5);
    	song.setArtist("Testament");
    	song.setTitle("3 Days In Darkness");
    	song.setAlbum("The gathering");
		tags = new HashSet<String>();
		tags.add("metal");
		tags.add("1999");
		song.setTags(tags);
		
		songList.add(song);
    }

    @After
    public void shutdown() throws InterruptedException {
        //Shutting down everything in an orderly fashion
		fs.getManager().destroy();
    }
}
