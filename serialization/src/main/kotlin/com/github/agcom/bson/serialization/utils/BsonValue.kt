package com.github.agcom.bson.serialization.utils

import org.bson.*
import org.bson.BsonType.*

// There is still a small chance of mis-predicting, e.g. when a `RawBsonValue` is a document and a primitive.
// unexpected will be called in case of unknown type or a deprecated/internal bson type.
internal inline fun <R> BsonValue.fold(
    primitive: (BsonValue) -> R = { unexpected(it) },
    document: (BsonDocument) -> R = { unexpected(it) },
    array: (BsonArray) -> R = { unexpected(it) },
    noinline unexpected: (BsonValue) -> R
): R {
    return when (bsonType) {
        ARRAY -> array(asArray()) // Array should be checked first as any array is a document but any document is not an array; Although maybe a primitive.
        DOCUMENT -> document(asDocument())
        DOUBLE, STRING, BINARY, OBJECT_ID, BOOLEAN, DATE_TIME, REGULAR_EXPRESSION, JAVASCRIPT, INT32, INT64, DECIMAL128, NULL ->
            primitive(this)
        else -> unexpected(this)
    }
}