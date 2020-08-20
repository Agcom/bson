package com.github.agcom.bson.serialization

import com.github.agcom.bson.serialization.decoders.readBson
import com.github.agcom.bson.serialization.encoders.writeBson
import com.github.agcom.bson.serialization.serializers.*
import com.github.agcom.bson.serialization.streaming.*
import com.github.agcom.bson.serialization.utils.RawBsonValue
import kotlinx.serialization.*
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.*
import org.bson.*
import org.bson.io.*
import org.bson.types.*
import java.nio.ByteBuffer

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
     * You can pass [BsonValueSerializer] as [deserializer] to get a [BsonValue].
     * Changes to [bytes] array won't reflect into the returned value.
     * In case of multiple possible bson types, a custom indirect child of [BsonValue] will be returned which functions' would work as much possible as expected. It's called [RawBsonValue] and extends [BsonBinary].
     */
    @OptIn(InternalSerializationApi::class)
    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val input by lazy { ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(bytes))) }
        val bson = when (deserializer.descriptor.kind) {
            // can only use `input.readBson`, but inferring by the needed type (when it's safe) is more trusted and efficient
            StructureKind.LIST -> input.use { it.readBsonArray() }
            is StructureKind -> input.use { it.readBsonDocument() }
            is PolymorphicKind -> {
                if (deserializer is AbstractPolymorphicSerializer) input.use { it.readBsonDocument() }
                else RawBsonValue.eager(bytes)
            }
            else -> RawBsonValue.eager(bytes)
        }
        return fromBson(deserializer, bson)
    }

    /**
     * This function was declared to semi-bypass the issue with inferring bytes bson type.
     * @param type The expected bson type of the [bytes]. E.g. [BsonType.DOCUMENT].
     */
    @Deprecated("Ports to the main load function; Issue was fixed", ReplaceWith("load(deserializer, bytes)"))
    fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray, type: BsonType): T {
        return load(deserializer, bytes)
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