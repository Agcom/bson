package com.github.agom.bson

import kotlinx.serialization.SerializationException

sealed class BsonException(final override val message: String) : SerializationException(message)

class BsonEncodingException(message: String) : BsonException(message)
class BsonDecodingException(message: String) : BsonException(message)