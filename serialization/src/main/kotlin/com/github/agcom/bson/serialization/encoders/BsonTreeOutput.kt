package com.github.agcom.bson.serialization.encoders

import com.github.agcom.bson.serialization.Bson
import com.github.agcom.bson.serialization.BsonEncodingException
import com.github.agcom.bson.serialization.serializers.BsonValueSerializer
import com.github.agcom.bson.serialization.utils.PRIMITIVE_TAG
import com.github.agcom.bson.serialization.utils.fold
import com.github.agcom.bson.serialization.utils.toBsonRegularExpression
import kotlinx.serialization.*
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.SerialModule
import org.bson.*
import org.bson.BsonType.*
import org.bson.types.*
import java.util.regex.Pattern

@OptIn(InternalSerializationApi::class)
private sealed class AbstractBsonTreeOutput(
    final override val bson: Bson,
    val nodeConsumer: (BsonValue) -> Unit
) : NamedValueEncoder(), BsonOutput {

    final override val context: SerialModule
        get() = bson.context

    private var writePolymorphic = false

    override fun encodeBson(element: BsonValue) = encodeSerializableValue(BsonValueSerializer, element)

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean =
        bson.configuration.encodeDefaults

    override fun composeName(parentName: String, childName: String): String = childName

    override fun encodeTaggedNull(tag: String) = putElement(tag, BsonNull.VALUE)
    override fun encodeTaggedInt(tag: String, value: Int) = putElement(tag, BsonInt32(value))
    override fun encodeTaggedByte(tag: String, value: Byte) = encodeTaggedInt(tag, value.toInt())
    override fun encodeTaggedShort(tag: String, value: Short) = encodeTaggedInt(tag, value.toInt())
    override fun encodeTaggedLong(tag: String, value: Long) = putElement(tag, BsonInt64(value))
    override fun encodeTaggedFloat(tag: String, value: Float) = encodeTaggedDouble(tag, value.toDouble())
    override fun encodeTaggedDouble(tag: String, value: Double) = putElement(tag, BsonDouble(value))
    override fun encodeTaggedBoolean(tag: String, value: Boolean) = putElement(tag, BsonBoolean(value))
    override fun encodeTaggedString(tag: String, value: String) = putElement(tag, BsonString(value))
    override fun encodeTaggedChar(tag: String, value: Char) = encodeTaggedString(tag, value.toString())
    override fun encodeTaggedEnum(tag: String, enumDescription: SerialDescriptor, ordinal: Int) =
        encodeTaggedString(tag, enumDescription.getElementName(ordinal))

    override fun encodeTaggedValue(tag: String, value: Any) = encodeTaggedString(tag, value.toString())

    private fun encodeTaggedBinary(tag: String, value: Binary) = putElement(tag, BsonBinary(value.type, value.data))
    private fun encodeTaggedObjectId(tag: String, value: ObjectId) = putElement(tag, BsonObjectId(value))
    private fun encodeTaggedDateTime(tag: String, value: Long) = putElement(tag, BsonDateTime(value))
    private fun encodeTaggedJavaScript(tag: String, value: String) = putElement(tag, BsonJavaScript(value))
    private fun encodeTaggedDecimal128(tag: String, value: Decimal128) = putElement(tag, BsonDecimal128(value))
    private fun encodeTaggedRegularExpression(tag: String, value: Pattern) =
        putElement(tag, value.toBsonRegularExpression())

    private fun encodeTaggedDbPointer(tag: String, value: BsonDbPointer) = putElement(tag, value)
    private fun encodeTaggerJavaScriptWithScope(tag: String, value: BsonJavaScriptWithScope) = putElement(tag, value)

    @Suppress("UNUSED_PARAMETER")
    private fun encodeTaggedMaxKey(tag: String, value: MaxKey) = putElement(tag, BsonMaxKey())

    @Suppress("UNUSED_PARAMETER")
    private fun encodeTaggedMinKey(tag: String, value: MinKey) = putElement(tag, BsonMinKey())
    private fun encodeTaggedSymbol(tag: String, value: String) = putElement(tag, BsonSymbol(value))

    private fun encodeTaggedBson(tag: String, value: BsonValue) = putElement(tag, value)

    override fun encodeBinary(binary: Binary) = encodeTaggedBinary(popTag(), binary)
    override fun encodeObjectId(objectId: ObjectId) = encodeTaggedObjectId(popTag(), objectId)
    override fun encodeDateTime(time: Long) = encodeTaggedDateTime(popTag(), time)
    override fun encodeJavaScript(code: String) = encodeTaggedJavaScript(popTag(), code)
    override fun encodeDecimal128(decimal: Decimal128) = encodeTaggedDecimal128(popTag(), decimal)
    override fun encodeRegularExpression(pattern: Pattern) = encodeTaggedRegularExpression(popTag(), pattern)
    override fun encodeDbPointer(pointer: BsonDbPointer) = encodeTaggedDbPointer(popTag(), pointer)
    override fun encodeJavaScriptWithScope(jsWithScope: BsonJavaScriptWithScope) =
        encodeTaggerJavaScriptWithScope(popTag(), jsWithScope)

    override fun encodeMaxKey(maxKey: MaxKey) = encodeTaggedMaxKey(popTag(), maxKey)
    override fun encodeMinKey(minKey: MinKey) = encodeTaggedMinKey(popTag(), minKey)
    override fun encodeSymbol(symbol: String) = encodeTaggedSymbol(popTag(), symbol)

    private fun checkClassDiscriminatorConflict(serializer: SerializationStrategy<*>) {
        if (bson.configuration.classDiscriminator in serializer.descriptor.elementNames())
            throw BsonEncodingException("Class discriminator '${bson.configuration.classDiscriminator}' conflict at ${serializer.descriptor.serialName}")
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {

        // Writing non-structured data (i.e. primitives) on top-level (e.g. without any tag) requires special output
        if (currentTagOrNull != null || (serializer.descriptor.kind !is PrimitiveKind && serializer.descriptor.kind !== UnionKind.ENUM_KIND)) {
            // Structured data
            if (serializer !is AbstractPolymorphicSerializer) serializer.serialize(this, value)
            else {
                @Suppress("UNCHECKED_CAST")
                val actualSerializer = serializer.findPolymorphicSerializer(this, value) as KSerializer<T>
                checkClassDiscriminatorConflict(actualSerializer)

                if (actualSerializer.descriptor.kind is PolymorphicKind)
                    throw BsonEncodingException("Actual serializer for polymorphic cannot be polymorphic itself")
                writePolymorphic = true
                actualSerializer.serialize(this, value)
            }

        } else BsonPrimitiveOutput(bson, nodeConsumer).apply {
            encode(serializer, value)
            endEncode(serializer.descriptor)
        }

    }

    override fun beginStructure(
        descriptor: SerialDescriptor,
        vararg typeSerializers: KSerializer<*>
    ): CompositeEncoder {
        val consumer = if (currentTagOrNull == null) nodeConsumer else { node -> putElement(currentTag, node) }
        val encoder = when (descriptor.kind) {
            StructureKind.LIST -> BsonTreeListOutput(bson, consumer)
            StructureKind.MAP -> BsonTreeMapOutput(bson, consumer)
            else -> BsonTreeOutput(bson, consumer)
        }
        if (writePolymorphic) {
            writePolymorphic = false
            encoder.putElement(bson.configuration.classDiscriminator, BsonString(descriptor.serialName))
        }
        return encoder
    }

    override fun endEncode(descriptor: SerialDescriptor) {
        nodeConsumer(getCurrent())
    }

    abstract fun putElement(key: String, element: BsonValue)
    abstract fun getCurrent(): BsonValue

}

private class BsonPrimitiveOutput(bson: Bson, nodeConsumer: (BsonValue) -> Unit) :
    AbstractBsonTreeOutput(bson, nodeConsumer) {

    private var content: BsonValue? = null

    init {
        pushTag(PRIMITIVE_TAG)
    }

    override fun putElement(key: String, element: BsonValue) {
        if (key !== PRIMITIVE_TAG) throw BsonEncodingException("This output can only consume primitives with '$PRIMITIVE_TAG' tag")
        if (content != null) throw BsonEncodingException("Primitive element was already recorded. Does call to encodeXxx happen more than once?")
        content = element
    }

    override fun getCurrent(): BsonValue = content
        ?: throw BsonEncodingException("Primitive element has not been recorded. Is call to encodeXxx is missing in serializer?")

}

private open class BsonTreeOutput(bson: Bson, nodeConsumer: (BsonValue) -> Unit) :
    AbstractBsonTreeOutput(bson, nodeConsumer) {

    protected val content: BsonDocument = BsonDocument()

    override fun putElement(key: String, element: BsonValue) {
        if (!bson.configuration.allowDuplicateKey && key in content) throw BsonEncodingException("Duplicate key '$key'")
        content[key] = element
    }

    override fun getCurrent(): BsonValue = content

}

private class BsonTreeMapOutput(bson: Bson, nodeConsumer: (BsonValue) -> Unit) : BsonTreeOutput(bson, nodeConsumer) {

    private lateinit var tag: String
    private var isKey = true

    override fun putElement(key: String, element: BsonValue) {
        if (isKey) { // Writing key
            element.fold(
                primitive = {
                    tag = when (it.bsonType) {
                        STRING -> it.asString().value
                        INT32 -> it.asInt32().value.toString()
                        INT64 -> it.asInt64().value.toString()
                        DOUBLE -> it.asDouble().value.toString()
                        OBJECT_ID -> it.asObjectId().value.toHexString()
                        DECIMAL128 -> it.asDecimal128().value.toString()
                        DATE_TIME -> it.asDateTime().value.toString()
                        BOOLEAN -> it.asBoolean().value.toString()
                        REGULAR_EXPRESSION -> it.asRegularExpression().pattern
                        JAVASCRIPT -> it.asJavaScript().code
                        NULL -> "null"
                        else -> throw RuntimeException("Should not reach here")
                    }
                }
            )
            isKey = false
        } else {
            super.putElement(tag, element)
            isKey = true
        }
    }

    override fun getCurrent(): BsonValue = content

    override fun shouldWriteElement(desc: SerialDescriptor, tag: String, index: Int): Boolean = true

}

private class BsonTreeListOutput(bson: Bson, nodeConsumer: (BsonValue) -> Unit) :
    AbstractBsonTreeOutput(bson, nodeConsumer) {

    private val array: BsonArray = BsonArray()

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()

    override fun shouldWriteElement(desc: SerialDescriptor, tag: String, index: Int): Boolean = true

    override fun putElement(key: String, element: BsonValue) {
        array.add(
            key.toIntOrNull()
                ?: throw BsonEncodingException("Array key cannot be non-integer. This should not normally happen"),
            element
        )
    }

    override fun getCurrent(): BsonValue = array

}

internal fun <T> Bson.writeBson(value: T, serializer: SerializationStrategy<T>): BsonValue {
    try {
        var result: BsonValue? = null
        val encoder = BsonTreeOutput(this) { result = it }
        encoder.encode(serializer, value)
        return result ?: throw BsonEncodingException("No value captured. Does your serializer calls endStructure?")
    } catch (ex: BSONException) {
        throw BsonEncodingException(ex.message ?: "", ex)
    }
}