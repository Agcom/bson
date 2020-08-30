package com.github.agcom.bson.serialization.utils

import org.bson.BsonBinary
import org.bson.types.Binary

// TODO("Continue here")

operator fun BsonBinary.invoke(binary: Binary): BsonBinary = binary.toBsonBinary()

fun Binary.toBsonBinary() = BsonBinary(type, data)
fun BsonBinary.toBinary() = Binary(type, data)