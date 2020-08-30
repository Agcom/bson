package com.github.agcom.bson.serialization.utils

import org.bson.*
import org.bson.io.BsonInput
import org.bson.io.BsonOutput

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