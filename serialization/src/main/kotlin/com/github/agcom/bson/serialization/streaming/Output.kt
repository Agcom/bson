package com.github.agcom.bson.serialization.streaming

import com.github.agcom.bson.serialization.utils.toBsonDocument
import org.bson.BsonArray
import org.bson.BsonBinaryWriter
import org.bson.BsonDocument
import org.bson.io.BsonOutput

internal fun BsonOutput.writeBsonDocument(doc: BsonDocument) {
    val writer = BsonBinaryWriter(this)
    doc.asBsonReader().use { reader ->
        writer.use {
            it.pipe(reader)
        }
    }
}

internal fun BsonOutput.writeBsonArray(array: BsonArray) {
    writeBsonDocument(array.toBsonDocument())
}