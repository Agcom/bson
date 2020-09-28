package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.encoders.BsonOutput
import kotlinx.serialization.*
import org.bson.BsonBinarySubType
import org.bson.UuidRepresentation
import org.bson.internal.UuidHelper
import org.bson.types.*
import java.util.*
import java.util.regex.Pattern

/**
 * [Binary] serializer.
 *
 * Corresponds to [BsonBinary][org.bson.BsonBinary] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeBinary] / [BsonInput.decodeBinary].
 */
@Serializer(Binary::class)
object BinarySerializer : KSerializer<Binary> {

    override val descriptor: SerialDescriptor = BsonBinarySerializer.descriptor

    override fun serialize(encoder: Encoder, value: Binary) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeBinary(value)
    }

    override fun deserialize(decoder: Decoder): Binary {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeBinary()
    }

}

/**
 * [ObjectId] serializer.
 *
 * Corresponds to [BsonObjectId][org.bson.BsonObjectId] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeObjectId] / [BsonInput.decodeObjectId].
 */
@Serializer(ObjectId::class)
object ObjectIdSerializer : KSerializer<ObjectId> {

    override val descriptor: SerialDescriptor = BsonObjectIdSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ObjectId) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeObjectId(value)
    }

    override fun deserialize(decoder: Decoder): ObjectId {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeObjectId()
    }

}

/**
 * Epoch millis time serializer.
 *
 * Corresponds to [BsonDateTime][org.bson.BsonDateTime] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeDateTime] / [BsonInput.decodeDateTime]
 */
@Serializer(Long::class)
object DateTimeSerializer : KSerializer<Long> {

    override val descriptor: SerialDescriptor = BsonDateTimeSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeDateTime(value)
    }

    override fun deserialize(decoder: Decoder): Long {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeDateTime()
    }

}

/**
 * Java script code serializer.
 *
 * Corresponds to [BsonJavaScript][org.bson.BsonJavaScript] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeJavaScript] / [BsonInput.decodeJavaScript]
 */
@Serializer(String::class)
object JavaScriptSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor = BsonJavaScriptSerializer.descriptor

    override fun serialize(encoder: Encoder, value: String) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeJavaScript(value)
    }

    override fun deserialize(decoder: Decoder): String {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeJavaScript()
    }

}

/**
 * [Decimal128] serializer.
 *
 * Corresponds to [BsonDecimal128][org.bson.BsonDecimal128] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeDecimal128] / [BsonInput.decodeDecimal128]
 */
@Serializer(Decimal128::class)
object Decimal128Serializer : KSerializer<Decimal128> {

    override val descriptor: SerialDescriptor = BsonDecimal128Serializer.descriptor

    override fun serialize(encoder: Encoder, value: Decimal128) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeDecimal128(value)
    }

    override fun deserialize(decoder: Decoder): Decimal128 {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeDecimal128()
    }

}

/**
 * [Pattern] serializer.
 *
 * Corresponds to [BsonRegularExpression][org.bson.BsonRegularExpression] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeRegularExpression] / [BsonInput.decodeRegularExpression]
 */
@Serializer(Pattern::class)
object PatternSerializer : KSerializer<Pattern> {

    override val descriptor: SerialDescriptor = BsonRegularExpressionSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Pattern) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeRegularExpression(value)
    }

    override fun deserialize(decoder: Decoder): Pattern {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeRegularExpression()
    }

}

/**
 * [Regex] serializer.
 *
 * Ports to [PatternSerializer]. Uses [toPattern] extension function when serializing and [toRegex] when deserializing.
 *
 * Corresponds to [BsonRegularExpression][org.bson.BsonRegularExpression] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeRegularExpression] / [BsonInput.decodeRegularExpression]
 */
@Serializer(Regex::class)
object RegexSerializer : KSerializer<Regex> {

    override val descriptor: SerialDescriptor = PatternSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Regex) {
        encoder.verify()
        encoder.encode(PatternSerializer, value.toPattern())
    }

    override fun deserialize(decoder: Decoder): Regex {
        decoder.verify()
        return decoder.decode(PatternSerializer).toRegex()
    }

}

/**
 * [Code] serializer.
 *
 * Ports to [JavaScriptSerializer] using [Code.code].
 *
 * Corresponds to [BsonJavaScript][org.bson.BsonJavaScript] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeJavaScript] / [BsonInput.decodeJavaScript]
 */
object CodeSerializer : KSerializer<Code> {

    override val descriptor: SerialDescriptor = JavaScriptSerializer.descriptor

    override fun deserialize(decoder: Decoder): Code {
        decoder.verify()
        return Code(decoder.decode(JavaScriptSerializer))
    }

    override fun serialize(encoder: Encoder, value: Code) {
        encoder.verify()
        encoder.encode(JavaScriptSerializer, value.code)
    }

}

/**
 * [ByteArray] serializer.
 *
 * Ports to [BinarySerializer] with [BsonBinarySubType.BINARY] as bson binary type.
 *
 * Corresponds to [BsonBinary][org.bson.BsonBinary] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeJavaScript] / [BsonInput.decodeJavaScript]
 */
object ByteArraySerializer : KSerializer<ByteArray> {

    override val descriptor: SerialDescriptor = BinarySerializer.descriptor

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.verify()
        encoder.encode(BinarySerializer, Binary(value))
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        decoder.verify()
        val binary = decoder.decode(BinarySerializer)
        if (binary.type != BsonBinarySubType.BINARY.value)
            throw SerializationException("Expected bson binary type to be ${BsonBinarySubType.BINARY}, " +
                    "but was ${BsonBinarySubType.values()
                        .firstOrNull { it.value == binary.type } ?: binary.type.toString(16)}")
        return binary.data
    }

}

/**
 * [UUID] serializer.
 *
 * Ports to [BinarySerializer] with [BsonBinarySubType.UUID_LEGACY] or [BsonBinarySubType.UUID_STANDARD] as bson binary type.
 *
 * @see UuidRepresentation
 *
 * Corresponds to [BsonBinary][org.bson.BsonBinary] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeJavaScript] / [BsonInput.decodeJavaScript]
 */
class UUIDSerializer(private val uuidRepresentation: UuidRepresentation = UuidRepresentation.STANDARD) :
    KSerializer<UUID> {

    override val descriptor: SerialDescriptor = BsonBinarySerializer.descriptor

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.verify()
        val binary = Binary(
            when (uuidRepresentation) {
                UuidRepresentation.STANDARD -> BsonBinarySubType.UUID_STANDARD
                else -> BsonBinarySubType.UUID_LEGACY
            },
            UuidHelper.encodeUuidToBinary(value, uuidRepresentation)
        )
        encoder.encode(BinarySerializer, binary)
    }

    override fun deserialize(decoder: Decoder): UUID {
        decoder.verify()
        val binary = decoder.decode(BinarySerializer)
        return UuidHelper.decodeBinaryToUuid(binary.data, binary.type, uuidRepresentation)
    }

}

/**
 * [Date] serializer.
 *
 * Ports to [DateTimeSerializer] using [Date.getTime] function.
 *
 * Corresponds to [BsonDateTime][org.bson.BsonDateTime] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [BsonOutput.encodeJavaScript] / [BsonInput.decodeJavaScript]
 */
object DateSerializer : KSerializer<Date> {

    override val descriptor: SerialDescriptor = DateTimeSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.verify()
        encoder.encode(DateTimeSerializer, value.time)
    }

    override fun deserialize(decoder: Decoder): Date {
        decoder.verify()
        return Date(decoder.decode(DateTimeSerializer))
    }

}

/**
 * [MaxKey] serializer.
 *
 * Corresponds to [BsonMaxKey][org.bson.BsonMaxKey] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object MaxKeySerializer : KSerializer<MaxKey> {

    override val descriptor: SerialDescriptor
        get() = BsonMaxKeySerializer.descriptor

    override fun serialize(encoder: Encoder, value: MaxKey) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeMaxKey(value)
    }

    override fun deserialize(decoder: Decoder): MaxKey {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeMaxKey()
    }

}

/**
 * [MinKey] serializer.
 *
 * Corresponds to [BsonMinKey][org.bson.BsonMinKey] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object MinKeySerializer : KSerializer<MinKey> {

    override val descriptor: SerialDescriptor
        get() = BsonMinKeySerializer.descriptor

    override fun serialize(encoder: Encoder, value: MinKey) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeMinKey(value)
    }

    override fun deserialize(decoder: Decoder): MinKey {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeMinKey()
    }

}

/**
 * Symbol string serializer.
 *
 * Corresponds to [BsonSymbol][org.bson.BsonSymbol] type.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object SymbolSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor
        get() = BsonSymbolSerializer.descriptor

    override fun serialize(encoder: Encoder, value: String) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeSymbol(value);
    }

    override fun deserialize(decoder: Decoder): String {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeSymbol()
    }

}