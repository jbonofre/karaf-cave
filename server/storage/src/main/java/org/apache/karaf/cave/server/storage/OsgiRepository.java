/*
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
package org.apache.karaf.cave.server.storage;

import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.apache.karaf.features.internal.repository.StaxParser;
import org.apache.karaf.features.internal.repository.XmlRepository;
import org.osgi.resource.Resource;

public class OsgiRepository extends XmlRepository {

    OsgiLoader loader;

    public OsgiRepository(String url, String name) {
        this(url);
        StaxParser.XmlRepository repository = new StaxParser.XmlRepository();
        repository.name = name;
        loader = new OsgiLoader(url, repository);
        getLoaders().put(url, loader);
    }

    public OsgiRepository(String url) {
        super(url);
    }

    public void addResource(Resource resource) {
        load();
        lock.writeLock().lock();
        try {
            loader.getXml().resources.add(resource);
            super.addResource(resource);
            loader.getXml().increment = System.currentTimeMillis();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public long getIncrement() {
        load();
        return loader.getXml().increment;
    }

    public void setIncrement(long increment) {
        load();
        loader.getXml().increment = increment;
    }

    public void writeRepository(Writer writer) throws XMLStreamException {
        StaxParser.write(loader.getXml(), writer);
    }

    private void load() {
        // Force repository load
        getResources();
    }

    protected static class OsgiLoader extends XmlLoader {
        public OsgiLoader(String url) {
            super(url);
        }

        public OsgiLoader(String url, StaxParser.XmlRepository xml) {
            super(url, xml);
        }

        public StaxParser.XmlRepository getXml() {
            return xml;
        }
    }

}