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
import java.nio.ByteBuffer

/**
 * Main entry point to work with BSON serialization.
 *
 * If some serializers are present in both [context] and the default bson context, serializers from [context] will take place.
 */
class Bson(
    val configuration: BsonConfiguration = BsonConfiguration.STABLE,
    context: SerialModule = EmptyModule
) : BinaryFormat {

    override val context: SerialModule = defaultBsonModule.overwriteWith(context)

    /**
     * Transform a [value] into a [BsonValue].
     */
    fun <T> toBson(serializer: SerializationStrategy<T>, value: T): BsonValue = writeBson(value, serializer)

    /**
     * Transform a [BsonValue] into an object.
     */
    fun <T> fromBson(deserializer: DeserializationStrategy<T>, bsonValue: BsonValue): T =
        readBson(bsonValue, deserializer)

    /**
     * Read a [BsonDocument] from the given [bytes].
     *
     * Note: Bson arrays are just documents with numerical consecutive indexes (starting from 0) as keys.
     * You can use [loadBsonArray] if you want to directly read an array, or use convertor function [toBsonArray] after reading a document.
     */
    fun loadBsonDocument(bytes: ByteArray): BsonDocument {
        return ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(bytes))).use {
            try {
                it.readBsonDocument()
            } catch (ex: BSONException) {
                throw BsonDecodingException(ex.message ?: "", ex)
            }
        }
    }

    /**
     * Read a [BsonArray] from the given [bytes].
     */
    fun loadBsonArray(bytes: ByteArray): BsonArray {
        return loadBsonDocument(bytes).toBsonArray() ?: throw BsonDecodingException("Not a bson array")
    }

    /**
     * Write a [BsonDocument] into a [ByteArray].
     */
    fun dumpBson(bsonDocument: BsonDocument): ByteArray {
        return BasicOutputBuffer().use {
            try {
                it.writeBsonDocument(bsonDocument);
            } catch (ex: BSONException) {
                throw BsonEncodingException(ex.message ?: "", ex)
            }
            it.toByteArray()
        }
    }

    /**
     * Write a [BsonArray] into a [ByteArray].
     */
    fun dumpBson(bsonArray: BsonArray): ByteArray {
        return dumpBson(bsonArray.toBsonDocument())
    }

    /**
     * Write [value] into a [ByteArray] according to bson format specification.
     *
     * Warning: Dumping primitives are not supported.
     */
    override fun <T> dump(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return toBson(serializer, value).fold(
            document = { dumpBson(it) },
            array = { dumpBson(it) },
            primitive = { throw BsonEncodingException("Dumping primitives") }
        )
    }

    /**
     * Read an object from the given [bytes] according to bson format specification.
     *
     * Warning: Loading primitives are not supported.
     */
    @OptIn(InternalSerializationApi::class)
    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val doc = loadBsonDocument(bytes)
        val array by lazy(mode = LazyThreadSafetyMode.NONE) { doc.toBsonArray() }

        fun trialAndError(): T {
            return try {
                fromBson(deserializer, doc)
            } catch (docEx: BsonException) { // Document failed
                try {
                    fromBson(deserializer, array ?: throw BsonDecodingException("Not a bson array"))
                } catch (arrayEx: BsonException) { // Array failed
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

}

private val defaultBsonModule: SerialModule = SerializersModule {
    contextual(BsonValueSerializer)
    contextual(BsonDocumentSerializer)
    contextual(BsonArraySerializer)
    contextual(BsonBinarySerializer)
    contextual(BsonBooleanSerializer)
    contextual(BsonDateTimeSerializer)
    contextual(BsonDecimal128Serializer)
    contextual(BsonDoubleSerializer)
    contextual(BsonInt32Serializer)
    contextual(BsonInt64Serializer)
    contextual(BsonJavaScriptSerializer)
    contextual(BsonNullSerializer)
    contextual(BsonNumberSerializer)
    contextual(BsonObjectIdSerializer)
    contextual(BsonRegularExpressionSerializer)
    contextual(BsonStringSerializer)
    contextual(BsonDbPointerSerializer)

    contextual(BinarySerializer)
    contextual(ObjectIdSerializer)
    contextual(Decimal128Serializer)
    contextual(RegexSerializer)
    contextual(PatternSerializer)
    contextual(CodeSerializer)
    contextual(ByteArraySerializer)
    contextual(UUIDSerializer())
    contextual(DateSerializer)

    include(bsonTemporalModule)
}