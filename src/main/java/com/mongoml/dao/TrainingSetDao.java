package com.mongoml.dao;

import com.mongoml.models.DbTrainingSet;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class TrainingSetDao extends BaseDao<DbTrainingSet> {

    public TrainingSetDao(Datastore datastore) {
        super(DbTrainingSet.class, datastore);
    }

    public List<DbTrainingSet> findAll() {
        final Query<DbTrainingSet> query = datastore.createQuery(DbTrainingSet.class);
        return query.asList();
    }
}
