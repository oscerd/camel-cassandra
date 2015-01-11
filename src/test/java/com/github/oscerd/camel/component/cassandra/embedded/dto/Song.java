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
package com.github.oscerd.camel.component.cassandra.embedded.dto;

import java.nio.ByteBuffer;
import java.util.Set;

public class Song {

    private int id;
    private String title;
    private String album;
    private String artist;
    private Set<String> tags;
    private ByteBuffer blob;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public ByteBuffer getBlob() {
        return blob;
    }

    public void setBlob(ByteBuffer blob) {
        this.blob = blob;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((album == null) ? 0 : album.hashCode());
        result = prime * result + ((artist == null) ? 0 : artist.hashCode());
        result = prime * result + ((blob == null) ? 0 : blob.hashCode());
        result = prime * result + id;
        result = prime * result + ((tags == null) ? 0 : tags.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Song other = (Song) obj;
        if (album == null) {
            if (other.album != null) {
                return false;
            }
        } else if (!album.equals(other.album)) {
            return false;
        }
        if (artist == null) {
            if (other.artist != null) {
                return false;
            }
        } else if (!artist.equals(other.artist)) {
            return false;
        }
        if (blob == null) {
            if (other.blob != null) {
                return false;
            }
        } else if (!blob.equals(other.blob)) {
            return false; 
        }
        if (id != other.id) {
            return false;
        }
        if (tags == null) {
            if (other.tags != null) {
                return false;
            }
        } else if (!tags.equals(other.tags)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Song [id=" + id + ", title=" + title + ", album=" + album
                + ", artist=" + artist + ", tags=" + tags + ", blob=" + blob
                + "]";
    }
}
