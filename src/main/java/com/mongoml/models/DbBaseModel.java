package com.mongoml.models;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Getter
@Setter
public abstract class DbBaseModel {

    @Id
    private ObjectId id;

    @Property("lastUpdated")
    private Long lastUpdated;

    @Property("created")
    private Long created;

}
