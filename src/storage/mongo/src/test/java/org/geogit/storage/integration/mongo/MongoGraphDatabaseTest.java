/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.integration.mongo;

import org.geogit.api.Platform;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.GraphDatabaseTest;
import org.geogit.storage.mongo.MongoConnectionManager;
import org.geogit.storage.mongo.MongoGraphDatabase;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoGraphDatabaseTest extends GraphDatabaseTest {
    @Override
    protected MongoGraphDatabase createDatabase(Platform platform) throws Exception {
        final IniMongoProperties properties = new IniMongoProperties();
        final String uri = properties.get("mongodb.uri", String.class).or("mongodb://localhost:27017/"); 
        final String database = properties.get("mongodb.database", String.class).or("geogit");
        MongoClient client = new MongoClient(new MongoClientURI(uri));
        DB db = client.getDB(database);
        db.dropDatabase();

        MongoConnectionManager manager = new MongoConnectionManager();
        ConfigDatabase config = new TestConfigDatabase(platform);
        MongoGraphDatabase mongoGraphDatabase = new MongoGraphDatabase(manager, config);
        return mongoGraphDatabase;
    }
}
