package com.github.agcom.bson.serialization.streaming

import com.github.agcom.bson.serialization.utils.toBsonArray
import org.bson.*
import org.bson.io.BsonInput

internal fun BsonInput.readBsonDocument(): BsonDocument {
    val doc = BsonDocument()
    val reader = BsonBinaryReader(this)
    BsonDocumentWriter(doc).use { writer ->
        reader.use {
            writer.pipe(it)
        }
    }
    return doc
}

internal fun BsonInput.readBsonArray(): BsonArray {
    return readBsonDocument().toBsonArray() ?: throw BsonSerializationException("Not a bson array")
}