package com.github.agcom.bson.serialization.utils

import com.github.agcom.bson.serialization.BsonDecodingException
import org.bson.BsonArray
import org.bson.BsonDocument
import org.bson.BsonType
import org.bson.BsonType.*
import org.bson.BsonValue

/**
 * Transform a [BsonDocument] into a [BsonArray].
 *
 * The document keys should be consecutive numbers starting from 0;
 *
 * @return null if the document is not representing an array.
 */
fun BsonDocument.toBsonArray(): BsonArray? {
    val arr = BsonArray()
    var counter = 0
    forEach { (key, value) ->
        val index = key.toIntOrNull() ?: return null
        if (index != counter++) return null
        arr.add(value)
    }
    return arr
}

/**
 * Transform a [BsonArray] into a [BsonDocument].
 *
 * The returned document keys will be array indexes starting from 0.
 */
fun BsonArray.toBsonDocument(): BsonDocument {
    val doc = BsonDocument()
    forEachIndexed { i, value ->
        doc[i.toString()] = value
    }
    return doc
}

// Unexpected will be called in case of unknown type or a deprecated/internal bson type.
internal inline fun <R> BsonValue.fold(
    noinline unexpected: (BsonValue) -> R = { unexpectedBsonType(it.bsonType) },
    primitive: (BsonValue) -> R = { unexpected(it) },
    document: (BsonDocument) -> R = { unexpected(it) },
    array: (BsonArray) -> R = { unexpected(it) }
): R {
    return when (bsonType) {
        ARRAY -> array(asArray()) // Array should be checked first as any array is a document but any document is not an array; Although the user might meant a document.
        DOCUMENT -> document(asDocument())
        DOUBLE, STRING, BINARY, OBJECT_ID, BOOLEAN, DATE_TIME, REGULAR_EXPRESSION, JAVASCRIPT, INT32, INT64, DECIMAL128, NULL, DB_POINTER, JAVASCRIPT_WITH_SCOPE, MAX_KEY, MIN_KEY ->
            primitive(this)
        else -> unexpected(this)
    }
}

internal fun unexpectedBsonType(type: BsonType): Nothing {
    throw BsonDecodingException("Unexpected bson type '$type'")
}