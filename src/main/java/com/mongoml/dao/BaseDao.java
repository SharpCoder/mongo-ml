package com.mongoml.dao;

import com.mongoml.models.DbBaseModel;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;

abstract class BaseDao<T extends DbBaseModel> {

    protected final Class<T> klass;
    protected final Datastore datastore;

    public BaseDao(Class<T> klass, Datastore datastore) {
        this.datastore = datastore;
        this.klass = klass;
    }

    public void create(T object) {
        object.setCreated(DateTime.now().getMillis());
        datastore.save(object);
    }

    public void delete(T object) {
        datastore.delete(object);
    }

    public T findById(ObjectId id) {
        return datastore.find(klass).field("id").equal("id").get();
    }

}
