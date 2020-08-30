package com.github.agcom.bson.serialization

import com.github.agcom.bson.serialization.decoders.readBson
import com.github.agcom.bson.serialization.encoders.writeBson
import com.github.agcom.bson.serialization.serializers.*
import com.github.agcom.bson.serialization.utils.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.*
import org.bson.*
import org.bson.io.BasicOutputBuffer
import org.bson.io.ByteBufferBsonInput
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
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

    fun loadBsonDocument(bytes: ByteArray): BsonDocument {
        return ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(bytes))).use { it.readBsonDocument() }
    }

    fun loadBsonArray(bytes: ByteArray): BsonArray {
        return loadBsonDocument(bytes).toBsonArray() ?: throw BsonDecodingException("Not a bson array")
    }

    fun dumpBson(bson: BsonDocument): ByteArray {
        return BasicOutputBuffer().use { it.writeBsonDocument(bson); it.toByteArray() }
    }

    fun dumpBson(bson: BsonArray): ByteArray {
        return dumpBson(bson.toBsonDocument())
    }

    /**
     * Transform [value] into a [ByteArray].
     * Dumping primitives are not supported.
     */
    override fun <T> dump(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return toBson(serializer, value).fold(
            document = {
                BasicOutputBuffer().use { output ->
                    output.writeBsonDocument(it)
                    output.toByteArray()
                }
            },
            array = {
                BasicOutputBuffer().use { output ->
                    output.writeBsonArray(it)
                    output.toByteArray()
                }
            },
            primitive = { throw BsonEncodingException("Dumping primitives") },
            unexpected = { throw BsonEncodingException("Unexpected bson type '${it.bsonType}'") }
        )
    }

    /**
     * Transform some [bytes] into a value.
     * Loading primitives are not supported.
     */
    @OptIn(InternalSerializationApi::class)
    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val input = ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(bytes)))
        val doc = input.use {
            it.readBsonDocument()
        }
        val array by lazy { doc.toBsonArray() }

        fun trialAndError(): T {
            // Trial and error
            return try {
                fromBson(deserializer, doc)
            } catch (docEx: BsonException) {
                try {
                    fromBson(deserializer, array ?: throw BsonDecodingException("Not a bson array"))
                } catch (arrayEx: BsonException) {
                    docEx.addSuppressed(arrayEx)
                    throw docEx
                }
            }
        }

        return when (deserializer.descriptor.kind) {
            StructureKind.LIST -> fromBson(deserializer, array ?: throw BsonDecodingException("Not a bson array"))
            is StructureKind -> fromBson(deserializer, doc)
            is PrimitiveKind, UnionKind.ENUM_KIND -> throw BsonDecodingException("Loading primitives")
            is PolymorphicKind -> {
                if (deserializer is AbstractPolymorphicSerializer) fromBson(deserializer, doc)
                else trialAndError()
            }
            UnionKind.CONTEXTUAL -> trialAndError()
        }
    }

    /**
     * This function was declared to semi-bypass the issue with inferring bytes bson type.
     * @param type The expected bson type of the [bytes]. E.g. [BsonType.DOCUMENT].
     */
    @Deprecated(
        "Ports to the main load function; Loading primitives are no longer supported",
        ReplaceWith("load(deserializer, bytes)")
    )
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