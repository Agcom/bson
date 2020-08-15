package com.github.agcom.bson.serialization.utils

import org.bson.BsonBinary
import org.bson.types.Binary

fun Binary.toBsonBinary() = BsonBinary(type, data)
fun BsonBinary.toBinary() = Binary(type, data)