package com.github.agcom.bson.serialization

class DefaultBsonInstanceTest : BsonInstanceTest {
    override val bson = Bson(BsonConfiguration.DEFAULT)
}

interface BsonInstanceTest {
    val bson: Bson
}