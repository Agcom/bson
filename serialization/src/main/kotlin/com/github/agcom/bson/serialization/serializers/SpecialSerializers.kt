package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.encoders.BsonOutput
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.regex.Pattern

/**
 * Uses [BsonOutput.encodeBinary] / [BsonInput.decodeBinary]
 */
@Serializer(Binary::class)
object BinarySerializer : KSerializer<Binary> {

    override val descriptor: SerialDescriptor = PrimitiveDescriptor(Binary::class.qualifiedName!!, PrimitiveKind.STRING)

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
 * Uses [BsonOutput.encodeObjectId] / [BsonInput.decodeObjectId]
 */
@Serializer(ObjectId::class)
object ObjectIdSerializer : KSerializer<ObjectId> {

    override val descriptor: SerialDescriptor = PrimitiveDescriptor(ObjectId::class.qualifiedName!!, PrimitiveKind.STRING)

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
 * Uses [BsonOutput.encodeDateTime] / [BsonInput.decodeDateTime]
 */
@Serializer(Long::class)
object DateTimeSerializer : KSerializer<Long> {

    override val descriptor: SerialDescriptor = Long.serializer().descriptor

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
 * Uses [BsonOutput.encodeJavaScript] / [BsonInput.decodeJavaScript]
 */
@Serializer(String::class)
object JavaScriptSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor = String.serializer().descriptor

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
 * Uses [BsonOutput.encodeDecimal128] / [BsonInput.decodeDecimal128]
 */
@Serializer(Decimal128::class)
object Decimal128Serializer : KSerializer<Decimal128> {

    override val descriptor: SerialDescriptor = PrimitiveDescriptor(Decimal128::class.qualifiedName!!, PrimitiveKind.STRING)

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
 * Uses [BsonOutput.encodeRegularExpression] / [BsonInput.decodeRegularExpression]
 */
@Serializer(Regex::class)
object RegexSerializer : KSerializer<Regex> {

    override val descriptor: SerialDescriptor = PrimitiveDescriptor(Regex::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Regex) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeRegularExpression(value)
    }

    override fun deserialize(decoder: Decoder): Regex {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeRegularExpression()
    }

}

/**
 * Ports to [RegexSerializer] using [toRegex] extension.
 */
@Serializer(Pattern::class)
object PatternSerializer : KSerializer<Pattern> {

    override val descriptor: SerialDescriptor = PrimitiveDescriptor(Pattern::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Pattern) = encoder.encode(RegexSerializer, value.toRegex())

    override fun deserialize(decoder: Decoder): Pattern = decoder.decode(RegexSerializer).toPattern()

}