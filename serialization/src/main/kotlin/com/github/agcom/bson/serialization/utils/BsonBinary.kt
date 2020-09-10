package com.github.agcom.bson.serialization.utils

import org.bson.BsonBinary
import org.bson.types.Binary

/**
 * Constructor like builder for [BsonBinary] using it's helper class [Binary].
 */
operator fun BsonBinary.invoke(binary: Binary): BsonBinary = binary.toBsonBinary()

/**
 * Convert a [BsonBinary] into it's helper class [Binary].
 */
fun Binary.toBsonBinary() = BsonBinary(type, data)

/**
 * Convert a [Binary] into it's bson type class [BsonBinary].
 */
fun BsonBinary.toBinary() = Binary(type, data)