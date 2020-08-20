package com.github.agcom.bson.serialization.streaming

import com.github.agcom.bson.serialization.utils.RawBsonValue
import org.bson.*
import org.bson.BsonType.*
import org.bson.io.BsonInput
import org.bson.types.Decimal128

internal fun BsonInput.readBson(): BsonValue = RawBsonValue.eager(this)

internal fun BsonInput.readBsonDocument(): BsonDocument {
    val doc = BsonDocument()
    val reader = BsonBinaryReader(this)
    BsonDocumentWriter(doc).use { writer ->
        reader.use {
            writer.pipe(it)
        }
    }
    return doc
}

internal fun BsonInput.readBsonArray(): BsonArray {
    val doc = readBsonDocument()
    val arr = BsonArray()
    var counter = 0
    doc.forEach { (key, value) ->
        val index = key.toIntOrNull() ?: throw BsonSerializationException("Not a bson array")
        if (index != counter++) throw BsonSerializationException("Not a bson array")
        arr.add(value)
    }
    return arr
}

internal fun BsonInput.readBsonJavaScript(): BsonJavaScript {
    return BsonJavaScript(readString())
}

internal fun BsonInput.readBsonInt64(): BsonInt64 {
    return BsonInt64(readInt64())
}

internal fun BsonInput.readBsonDateTime(): BsonDateTime {
    return BsonDateTime(readInt64())
}

internal fun BsonInput.readBsonBoolean(): BsonBoolean {
    return BsonBoolean(
        when (readByte()) {
            1.toByte() -> true
            0.toByte() -> false
            else -> throw BsonSerializationException("Not a '$BOOLEAN'")
        }
    )
}

internal fun BsonInput.readBsonBinary(): BsonBinary {
    var size = readInt32()
    if (size < 0) throw BsonSerializationException("Invalid binary data size '$size'")
    val binaryType = readByte()
    if (binaryType == BsonBinarySubType.OLD_BINARY.value) {
        val repeatedSize: Int = readInt32()
        if (repeatedSize != size - 4) throw BsonSerializationException("Binary sub type OldBinary has inconsistent sizes")
        size -= 4
    }
    val bytes = ByteArray(size)
    readBytes(bytes)
    return BsonBinary(binaryType, bytes)
}

internal fun BsonInput.readBsonInt32(): BsonInt32 {
    return BsonInt32(readInt32())
}

internal fun BsonInput.readBsonString(): BsonString {
    return BsonString(readString())
}

internal fun BsonInput.readBsonDouble(): BsonDouble {
    return BsonDouble(readDouble())
}

internal fun BsonInput.readBsonObjectId(): BsonObjectId {
    return BsonObjectId(readObjectId())
}

internal fun BsonInput.readBsonRegularExpression(): BsonRegularExpression {
    return BsonRegularExpression(readCString(), readCString())
}

internal fun BsonInput.readBsonDecimal128(): BsonDecimal128 {
    val low = readInt64()
    val high = readInt64()
    return BsonDecimal128(Decimal128.fromIEEE754BIDEncoding(high, low))
}

internal fun BsonInput.readBsonNull(): BsonNull {
    return BsonNull.VALUE
}