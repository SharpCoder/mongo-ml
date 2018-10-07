package com.mongoml.dao;

import com.mongodb.MongoClient;
import com.mongoml.configuration.MongoConfiguration;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class MongoManager {

    private final MongoClient mongoClient;
    private final Datastore datastore;

    public MongoManager(MongoConfiguration mongoConfiguration, String database) {

        // Setup the client
        if (mongoConfiguration == null) {
            mongoClient = new MongoClient();
        } else {
            mongoClient = new MongoClient(mongoConfiguration.getHost(), mongoConfiguration.getPort());
        }

        // Setup morphia
        final Morphia morphia = new Morphia();
        morphia.mapPackage("com.mongoml.models");

        // Create datastore and ensure indexes on it
        datastore = morphia.createDatastore(mongoClient, database);
        datastore.ensureIndexes();
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
