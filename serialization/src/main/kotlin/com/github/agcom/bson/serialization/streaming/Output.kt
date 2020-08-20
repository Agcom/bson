package com.github.agcom.bson.serialization.streaming

import com.github.agcom.bson.serialization.BsonEncodingException
import com.github.agcom.bson.serialization.utils.*
import org.bson.*
import org.bson.io.BsonOutput

internal fun BsonOutput.writeBson(bson: BsonValue) {
    bson.fold(
        primitive = { writePrimitive(it) },
        document = { writeDocument(it) },
        array = { writeArray(it) },
        unexpected = { throw BsonEncodingException("Unexpected bson type '${it.bsonType}'") }
    )
}

private fun BsonOutput.writePrimitive(bson: BsonValue) {
    bson.fold(
        primitive = {
            when {
                it.isDouble -> writeDouble(it.asDouble().value)
                it.isString -> writeString(it.asString().value)
                it.isBinary -> {
                    it.asBinary(); it as BsonBinary
                    var totalLen: Int = it.data.size
                    if (it.type == BsonBinarySubType.OLD_BINARY.value) totalLen += 4
                    writeInt32(totalLen)
                    writeByte(it.type.toInt())
                    if (it.type == BsonBinarySubType.OLD_BINARY.value) writeInt32(totalLen - 4)
                    writeBytes(it.data)
                }
                it.isObjectId -> writeObjectId(it.asObjectId().value)
                it.isBoolean -> writeByte(if (it.asBoolean().value) 1 else 0)
                it.isDateTime -> writeInt64(it.asDateTime().value)
                it.isNull -> { /* No op */ }
                it.isRegularExpression -> {
                    it.asRegularExpression(); it as BsonRegularExpression
                    writeCString(it.pattern)
                    writeCString(it.options)
                }
                it.isJavaScript -> writeString(it.asJavaScript().code)
                it.isInt32 -> writeInt32(it.asInt32().value)
                it.isInt64 -> writeInt64(it.asInt64().value)
                it.isDecimal128 -> {
                    it.asDecimal128(); it as BsonDecimal128
                    writeInt64(it.value.low)
                    writeInt64(it.value.high)
                }
                else -> throw BsonEncodingException("Unexpected bson type '${it.bsonType}'")
            }
        },
        unexpected = { throw BsonEncodingException("Unexpected bson type '${it.bsonType}'") }
    )
}

private fun BsonOutput.writeDocument(doc: BsonDocument) {
    val writer = BsonBinaryWriter(this)
    BsonDocumentReader(doc).use { reader ->
        writer.use {
            it.pipe(reader)
        }
    }
}

private fun BsonOutput.writeArray(array: BsonArray) {
    val doc = BsonDocument()
    array.forEachIndexed { i, value ->
        doc[i.toString()] = value
    }
    writeDocument(doc)
    /*// Old procedure
    val startPosition = position
    writeInt32(0) // reserve space for size
    array.forEachIndexed { i, it ->
    writeByte(it.bsonType.value)
    writeCString(i.toString())
    writeBson(it)
    }
    writeByte(END_OF_DOCUMENT.value)

    val size = position - startPosition
    writeInt32(startPosition, size)
    */
}