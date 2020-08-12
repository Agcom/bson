package com.github.agcom.bson.streaming

import com.github.agcom.bson.BsonDecodingException
import org.bson.*
import org.bson.io.BsonInput
import org.bson.types.Decimal128

internal fun BsonInput.readBson(type: BsonType): BsonValue {
    return when (type) {
        BsonType.DOUBLE, BsonType.STRING, BsonType.BINARY, BsonType.OBJECT_ID, BsonType.BOOLEAN, BsonType.DATE_TIME, BsonType.NULL, BsonType.REGULAR_EXPRESSION, BsonType.JAVASCRIPT, BsonType.INT32, BsonType.INT64, BsonType.DECIMAL128 ->
            readPrimitive(type)
        BsonType.DOCUMENT -> readDocument()
        BsonType.ARRAY -> readArray()
        else -> throw BsonDecodingException("Unexpected bson type '$type'")
    }
}

private fun BsonInput.readPrimitive(type: BsonType): BsonValue {
    val value: BsonValue = when (type) {
        BsonType.DOUBLE -> BsonDouble(readDouble())
        BsonType.STRING -> BsonString(readString())
        BsonType.BINARY -> {
            var size = readInt32()
            if (size < 0) throw BsonDecodingException("Invalid binary data size '$size'")
            val binaryType = readByte()
            if (binaryType == BsonBinarySubType.OLD_BINARY.value) {
                val repeatedSize: Int = readInt32()
                if (repeatedSize != size - 4) throw BsonDecodingException("Binary sub type OldBinary has inconsistent sizes")
                size -= 4
            }
            val bytes = ByteArray(size)
            readBytes(bytes)
            BsonBinary(binaryType, bytes)
        }
        BsonType.OBJECT_ID -> BsonObjectId(readObjectId())
        BsonType.BOOLEAN -> {
            BsonBoolean(
                when (readByte()) {
                    1.toByte() -> true
                    0.toByte() -> false
                    else -> throw BsonDecodingException("Not a bson boolean")
                }
            )
        }
        BsonType.DATE_TIME -> BsonDateTime(readInt64())
        BsonType.NULL -> BsonNull.VALUE
        BsonType.REGULAR_EXPRESSION -> BsonRegularExpression(readCString(), readCString())
        BsonType.JAVASCRIPT -> BsonJavaScript(readString())
        BsonType.INT32 -> BsonInt32(readInt32())
        BsonType.INT64 -> BsonInt64(readInt64())
        BsonType.DECIMAL128 -> {
            val low = readInt64()
            val high = readInt64()
            return BsonDecimal128(Decimal128.fromIEEE754BIDEncoding(high, low))
        }
        else -> throw BsonDecodingException("Unexpected bson type '$type'")
    }
    if (hasRemaining()) throw BsonDecodingException("Not a bson primitive")
    return value
}

private fun BsonInput.readDocument(): BsonDocument {
    val doc = BsonDocument()
    val reader = BsonBinaryReader(this)
    BsonDocumentWriter(doc).use { writer ->
        reader.use {
            writer.pipe(it)
        }
    }
    return doc
}

private fun BsonInput.readArray(): BsonArray {
    val doc = readDocument()
    val arr = BsonArray()
    var counter = 0
    doc.forEach { (key, value) ->
        val index = key.toIntOrNull() ?: throw BsonDecodingException("Not a bson array")
        if (index != counter++) throw BsonDecodingException("Not a bson array")
        arr.add(value)
    }
    return arr
}