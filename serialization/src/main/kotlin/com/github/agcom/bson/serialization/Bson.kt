package com.github.agcom.bson.serialization

import com.github.agcom.bson.serialization.decoders.readBson
import com.github.agcom.bson.serialization.encoders.writeBson
import com.github.agcom.bson.serialization.serializers.*
import com.github.agcom.bson.serialization.streaming.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.*
import org.bson.*
import org.bson.io.*
import org.bson.types.*
import java.nio.ByteBuffer
import java.util.regex.Pattern

/**
 * Main entry point to work with BSON serialization.
 */
class Bson(
    val configuration: BsonConfiguration = BsonConfiguration.STABLE,
    context: SerialModule = EmptyModule
) : BinaryFormat {

    override val context: SerialModule = context + defaultBsonModule

    /**
     * Transform [value] into a [BsonValue].
     */
    fun <T> toBson(serializer: SerializationStrategy<T>, value: T): BsonValue = writeBson(value, serializer)

    /**
     * Transform a [BsonValue] into an object.
     */
    fun <T> fromBson(deserializer: DeserializationStrategy<T>, bson: BsonValue): T = readBson(bson, deserializer)

    /**
     * Transform [value] into a [ByteArray].
     */
    override fun <T> dump(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val bson = toBson(serializer, value)
        return BasicOutputBuffer().use {
            it.writeBson(bson)
            it.toByteArray()
        }
    }

    /**
     * Transform some [bytes] into a value.
     * You can pas [BsonValueSerializer] as [deserializer] to get a [BsonValue].
     * It's always safer to use the other signature [load] below.
     */
    @OptIn(InternalSerializationApi::class)
    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val type: BsonType = when (deserializer.descriptor.kind) {
            StructureKind.LIST -> BsonType.ARRAY
            is StructureKind -> BsonType.DOCUMENT
            is PolymorphicKind -> {
                if (deserializer is AbstractPolymorphicSerializer) BsonType.DOCUMENT
                else throw BsonDecodingException(
                    "Unable to infer the bytes bson type\n" +
                            "Supply the bson type using load(deserializer, bytes, type) function if you're sure about the bytes bson type, else this is a bug and is filed for fix"
                )
            }
            else -> throw BsonDecodingException(
                "Unable to infer the bytes bson type\n" +
                        "Supply the bson type using load(deserializer, bytes, type) function if you're sure about the bytes bson type, else this is a bug and is filed for fix"
            )
        }
        return load(deserializer, bytes, type)
    }

    /**
     * This function was declared to semi-bypass the issue with inferring bytes bson type.
     * It's always safer to use this signature instead of the main one.
     * Still some issue remains, like you can't read a bson primitive not knowing [bytes] exact type.
     * @param type The expected bson type of the [bytes]. E.g. [BsonType.DOCUMENT].
     */
    fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray, type: BsonType): T {
        val bson = ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(bytes))).use {
            it.readBson(type)
        }
        return fromBson(deserializer, bson)
    }

}

private val bsonTypesModule: SerialModule = serializersModuleOf(
    mapOf(
        BsonValue::class to BsonValueSerializer,
        BsonDocument::class to BsonDocumentSerializer,
        BsonArray::class to BsonArraySerializer,
        BsonBinary::class to BsonPrimitiveSerializer,
        BsonBoolean::class to BsonPrimitiveSerializer,
        BsonDateTime::class to BsonPrimitiveSerializer,
        BsonDecimal128::class to BsonPrimitiveSerializer,
        BsonDouble::class to BsonPrimitiveSerializer,
        BsonInt32::class to BsonPrimitiveSerializer,
        BsonInt64::class to BsonPrimitiveSerializer,
        BsonJavaScript::class to BsonPrimitiveSerializer,
        BsonNull::class to BsonPrimitiveSerializer,
        BsonNumber::class to BsonPrimitiveSerializer,
        BsonObjectId::class to BsonPrimitiveSerializer,
        BsonRegularExpression::class to BsonPrimitiveSerializer,
        BsonString::class to BsonPrimitiveSerializer,
        Binary::class to BinarySerializer,
        ObjectId::class to ObjectIdSerializer,
        Decimal128::class to Decimal128Serializer
    )
)

private val defaultBsonModule: SerialModule = SerializersModule {
    include(bsonTypesModule)
    include(bsonTemporalModule)
    contextual(RegexSerializer)
    contextual(PatternSerializer)
}