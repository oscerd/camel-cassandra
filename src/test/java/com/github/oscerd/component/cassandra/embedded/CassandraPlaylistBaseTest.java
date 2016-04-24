/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.oscerd.component.cassandra.embedded;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import io.teknek.farsandra.Farsandra;
import io.teknek.farsandra.LineHandler;
import io.teknek.farsandra.ProcessHandler;

import com.github.oscerd.component.cassandra.embedded.dto.Song;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Sample on how to spin up an embedded Cassandra instance
 * using in-memory storage.
 */
public class CassandraPlaylistBaseTest extends CamelTestSupport {
    
    private Farsandra fs;

    @Override
    public void doPostSetup() {
        String id = RandomStringUtils.random(12,
                "0123456789abcdefghijklmnopqrstuvwxyz");
        fs = new Farsandra();
        fs.withVersion("3.5");
        fs.withCleanInstanceOnStart(true);
        fs.withInstanceName("target" + File.separator + id);
        fs.withCreateConfigurationFiles(true);
        fs.withHost("localhost");
        fs.withSeeds(Arrays.asList("localhost"));
        final CountDownLatch started = new CountDownLatch(1);
        fs.getManager().addOutLineHandler(new LineHandler() {
            @Override
            public void handleLine(String line) {
                if (line.contains("Listening for thrift clients...")) {
                    started.countDown();
                }
            }
        });
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
                        + "= {'class':'SimpleStrategy', 'replication_factor':1};");
        session.execute("CREATE TABLE simplex.songs (id uuid PRIMARY KEY, title text, album text, artist text, data blob);");
        session.execute("CREATE TABLE simplex.playlists ( id uuid, song_order int, song_id uuid, title text, album text, artist text, PRIMARY KEY  (id, song_order ) );");
        session.execute("CREATE INDEX IF NOT EXISTS artist_idx ON simplex.playlists(artist);");
        session.execute("INSERT INTO simplex.playlists (id, song_order, song_id, title, artist, album) VALUES (62c36092-82a1-3a00-93d1-46196ee77204, 1, a3e64f8f-bd44-4f28-b8d9-6938726e34d4, 'La Grange', 'ZZ Top', 'Tres Hombres');");
        session.execute("INSERT INTO simplex.playlists (id, song_order, song_id, title, artist, album) VALUES (62c36092-82a1-3a00-93d1-46196ee77204, 2, 8a172618-b121-4136-bb10-f665cfc469eb, 'Moving in Stereo', 'Fu Manchu', 'We Must Obey');");
        session.execute("INSERT INTO simplex.playlists (id, song_order, song_id, title, artist, album) VALUES (62c36092-82a1-3a00-93d1-46196ee77204, 3, 2b09185b-fb5a-4734-9b56-49077de9edbf, 'Outside Woman Blues', 'Back Door Slam', 'Roll Away');");
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
        fs.getManager().destroyAndWaitForShutdown(10);
        super.tearDown();
    }
}
