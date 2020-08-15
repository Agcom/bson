package com.github.agcom.bson.serialization.streaming

import com.github.agcom.bson.serialization.BsonEncodingException
import org.bson.*
import org.bson.BsonType.*
import org.bson.io.BsonOutput

internal fun BsonOutput.writeBson(bson: BsonValue) {
    when (bson.bsonType) {
        DOUBLE, STRING, BINARY, OBJECT_ID, BOOLEAN, DATE_TIME, NULL, REGULAR_EXPRESSION, JAVASCRIPT, INT32, INT64, DECIMAL128 ->
            writePrimitive(bson)
        DOCUMENT -> writeDocument(bson.asDocument())
        ARRAY -> writeArray(bson.asArray())
        else -> throw BsonEncodingException("Unexpected bson type '${bson.bsonType}'")
    }
}

private fun BsonOutput.writePrimitive(bson: BsonValue) {
    when (bson.bsonType) {
        DOUBLE -> writeDouble(bson.asDouble().value)
        STRING -> writeString(bson.asString().value)
        BINARY -> {
            bson.asBinary(); bson as BsonBinary
            var totalLen: Int = bson.data.size
            if (bson.type == BsonBinarySubType.OLD_BINARY.value) totalLen += 4
            writeInt32(totalLen)
            writeByte(bson.type.toInt())
            if (bson.type == BsonBinarySubType.OLD_BINARY.value) writeInt32(totalLen - 4)
            writeBytes(bson.data)
        }
        OBJECT_ID -> writeObjectId(bson.asObjectId().value)
        BOOLEAN -> writeByte(if (bson.asBoolean().value) 1 else 0)
        DATE_TIME -> writeInt64(bson.asDateTime().value)
        NULL -> { /* Nothing */
        }
        REGULAR_EXPRESSION -> {
            bson.asRegularExpression(); bson as BsonRegularExpression
            writeCString(bson.pattern)
            writeCString(bson.options)
        }
        JAVASCRIPT -> writeString(bson.asJavaScript().code)
        INT32 -> writeInt32(bson.asInt32().value)
        INT64 -> writeInt64(bson.asInt64().value)
        DECIMAL128 -> {
            bson.asDecimal128(); bson as BsonDecimal128
            writeInt64(bson.value.low)
            writeInt64(bson.value.high)
        }
        else -> throw BsonEncodingException("Unexpected bson type '${bson.bsonType}'")
    }
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