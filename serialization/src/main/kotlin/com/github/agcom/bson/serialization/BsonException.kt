package com.github.agcom.bson.serialization

import kotlinx.serialization.SerializationException

/**
 * Generic exception indicating a problem in the serialization or deserialization process.
 *
 * Don't confuse it with [org.bson.BSONException], although it also may be thrown (issue #12).
 */
sealed class BsonException(final override val message: String) : SerializationException(message)

/**
 * A problem in the serialization process.
 */
class BsonEncodingException(message: String) : BsonException(message)

/**
 * A problem in the deserialization process.
 */
class BsonDecodingException(message: String) : BsonException(message)