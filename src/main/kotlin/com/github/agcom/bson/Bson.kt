package com.github.agcom.bson

import com.github.agcom.bson.decoders.readBson
import com.github.agcom.bson.encoders.writeBson
import com.github.agcom.bson.serializers.*
import com.github.agcom.bson.streaming.readBson
import com.github.agcom.bson.streaming.writeBson
import kotlinx.serialization.*
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.float
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.serializersModuleOf
import org.bson.BsonType
import org.bson.BsonValue
import org.bson.ByteBufNIO
import org.bson.io.BasicOutputBuffer
import org.bson.io.ByteBufferBsonInput
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.nio.ByteBuffer
import java.util.regex.Pattern

class Bson(
    val configuration: BsonConfiguration = BsonConfiguration.STABLE,
    context: SerialModule = EmptyModule
) : BinaryFormat {

    override val context: SerialModule = context + defaultBsonModule

    fun <T> toBson(serializer: SerializationStrategy<T>, value: T): BsonValue = writeBson(value, serializer)

    fun <T> fromBson(deserializer: DeserializationStrategy<T>, bson: BsonValue): T = readBson(bson, deserializer)

    override fun <T> dump(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val bson = toBson(serializer, value)
        return BasicOutputBuffer().use {
            it.writeBson(bson)
            it.toByteArray()
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val type: BsonType = when(deserializer.descriptor.kind) {
            PrimitiveKind.BOOLEAN -> BsonType.BOOLEAN // Safe
            PrimitiveKind.INT, PrimitiveKind.BYTE, PrimitiveKind.SHORT -> BsonType.INT32 // Safe
            PrimitiveKind.STRING, PrimitiveKind.CHAR, UnionKind.ENUM_KIND -> BsonType.STRING // Safe
            PrimitiveKind.LONG -> BsonType.INT64 // Safe
            PrimitiveKind.DOUBLE, PrimitiveKind.FLOAT -> BsonType.DOUBLE // Safe
            StructureKind.CLASS, StructureKind.MAP, StructureKind.OBJECT -> BsonType.DOCUMENT // Safe
            StructureKind.LIST -> BsonType.ARRAY // Safe
            UnionKind.CONTEXTUAL -> throw BsonDecodingException("Unable to detect bytes bson type\nuse load(deserializer, bytes, type) if you're sure about the bytes bson type, else this is an issue and is filed for fix") // Unsafe
            is PolymorphicKind -> {
                if(deserializer is AbstractPolymorphicSerializer) BsonType.DOCUMENT // Safe
                else throw BsonDecodingException("Unable to detect bytes bson type\nuse load(deserializer, bytes, type) if you're sure about the bytes bson type, else this is a bug and is filed for fix") // Unsafe
            }
            else -> throw BsonDecodingException("Unexpected kind '${deserializer.descriptor.kind}'")
        }
        return load(deserializer, bytes, type)
    }

    fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray, type: BsonType): T {
        val bson = ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(bytes))).use {
            it.readBson(type)
        }
        return fromBson(deserializer, bson)
    }

}

private val defaultBsonModule: SerialModule = serializersModuleOf(
    mapOf(
        BsonValue::class to BsonValueSerializer,
        Binary::class to BinarySerializer,
        ObjectId::class to ObjectIdSerializer,
        Decimal128::class to Decimal128Serializer,
        Regex::class to RegexSerializer,
        Pattern::class to PatternSerializer
    )
)

fun main() {
    println(Json.parseJson("""
        "10"
    """.trimIndent()).float)
}