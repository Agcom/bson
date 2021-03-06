package com.github.agcom.bson.serialization.decoders

import com.github.agcom.bson.serialization.Bson
import com.github.agcom.bson.serialization.BsonDecodingException
import com.github.agcom.bson.serialization.utils.PRIMITIVE_TAG
import com.github.agcom.bson.serialization.utils.fold
import com.github.agcom.bson.serialization.utils.toBinary
import com.github.agcom.bson.serialization.utils.toPattern
import kotlinx.serialization.*
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.modules.SerialModule
import org.bson.*
import org.bson.types.*
import java.util.regex.Pattern

@OptIn(InternalSerializationApi::class)
private sealed class AbstractBsonTreeInput(
    override val bson: Bson,
    open val value: BsonValue
) : NamedValueDecoder(), BsonInput {

    override val context: SerialModule
        get() = bson.context

    private fun currentObject(): BsonValue = currentTagOrNull?.let { currentElement(it) } ?: value

    override fun decodeBson(): BsonValue = currentObject()

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return if (deserializer is AbstractPolymorphicSerializer) {
            val bsonTree = decodeBson().asDocument()

            val type = bsonTree.remove(bson.configuration.classDiscriminator)?.asString()?.value
                ?: throw BsonDecodingException("Class discriminator is missing")

            val actualSerializer = deserializer.findPolymorphicSerializer(this, type)
            bson.readBson(bsonTree, actualSerializer)
        } else deserializer.deserialize(this)
    }

    override fun composeName(parentName: String, childName: String): String = childName

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        val currentObject = currentObject()
        return when (descriptor.kind) {
            StructureKind.LIST -> BsonTreeListInput(bson, currentObject.asArray())
            StructureKind.MAP -> BsonTreeMapInput(bson, currentObject.asDocument())
            else -> BsonTreeInput(bson, currentObject.asDocument())
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // Nothing
    }

    protected open fun getValue(tag: String): BsonValue {
        val currentElement = currentElement(tag)
        return currentElement.fold(
            primitive = { it }
        )
    }

    protected abstract fun currentElement(tag: String): BsonValue

    override fun decodeBinary(): Binary = decodeTaggedBinary(popTag())
    override fun decodeObjectId(): ObjectId = decodeTaggedObjectId(popTag())
    override fun decodeDateTime(): Long = decodeTaggedDateTime(popTag())
    override fun decodeJavaScript(): String = decodeTaggedJavaScript(popTag())
    override fun decodeDecimal128(): Decimal128 = decodeTaggedDecimal128(popTag())
    override fun decodeRegularExpression(): Pattern = decodeTaggedRegularExpression(popTag())
    override fun decodeDbPointer(): BsonDbPointer = decodeTaggedDbPointer(popTag())
    override fun decodeJavaScriptWithScope(): BsonJavaScriptWithScope = decodeTaggedJavaScriptWithScope(popTag())
    override fun decodeMaxKey(): MaxKey = decodeTaggedMaxKey(popTag())
    override fun decodeMinKey(): MinKey = decodeTaggedMinKey(popTag())
    override fun decodeSymbol(): String = decodeTaggedSymbol(popTag())
    override fun decodeUndefined(): BsonUndefined = decodeTaggedUndefined(popTag())
    override fun decodeTimestamp(): BsonTimestamp = decodeTaggedTimestamp(popTag())

    override fun decodeTaggedEnum(tag: String, enumDescription: SerialDescriptor): Int =
        enumDescription.getElementIndexOrThrow(getValue(tag).asString().value)

    override fun decodeTaggedNull(tag: String): Nothing? = null
    override fun decodeTaggedNotNullMark(tag: String): Boolean = currentElement(tag) !== BsonNull.VALUE
    override fun decodeTaggedBoolean(tag: String): Boolean = getValue(tag).asBoolean().value
    override fun decodeTaggedLong(tag: String) = getValue(tag).asInt64().value
    override fun decodeTaggedInt(tag: String) = getValue(tag).asInt32().value
    override fun decodeTaggedByte(tag: String) = decodeTaggedInt(tag).toByte()
    override fun decodeTaggedShort(tag: String) = decodeTaggedInt(tag).toShort()
    override fun decodeTaggedDouble(tag: String) = getValue(tag).asDouble().value
    override fun decodeTaggedFloat(tag: String) = decodeTaggedDouble(tag).toFloat()
    override fun decodeTaggedChar(tag: String) = getValue(tag).asString().value[0]
    override fun decodeTaggedString(tag: String): String = getValue(tag).asString().value
    private fun decodeTaggedBinary(tag: String): Binary = getValue(tag).asBinary().toBinary()
    private fun decodeTaggedObjectId(tag: String): ObjectId = getValue(tag).asObjectId().value
    private fun decodeTaggedDateTime(tag: String): Long = getValue(tag).asDateTime().value
    private fun decodeTaggedJavaScript(tag: String): String = getValue(tag).asJavaScript().code
    private fun decodeTaggedDecimal128(tag: String): Decimal128 = getValue(tag).asDecimal128().value
    private fun decodeTaggedRegularExpression(tag: String): Pattern = getValue(tag).asRegularExpression().toPattern()
    private fun decodeTaggedDbPointer(tag: String): BsonDbPointer = getValue(tag).asDBPointer()
    private fun decodeTaggedJavaScriptWithScope(tag: String): BsonJavaScriptWithScope =
        getValue(tag).asJavaScriptWithScope()

    private fun decodeTaggedMaxKey(tag: String): MaxKey = getValue(tag).let {
        it as? BsonMaxKey
            ?: throw BsonInvalidOperationException("Value expected to be of type ${BsonType.MAX_KEY} is of unexpected type ${it.bsonType}")
        MaxKey()
    }

    private fun decodeTaggedMinKey(tag: String): MinKey = getValue(tag).let {
        it as? BsonMinKey
            ?: throw BsonInvalidOperationException("Value expected to be of type ${BsonType.MIN_KEY} is of unexpected type ${it.bsonType}")
        MinKey()
    }

    private fun decodeTaggedSymbol(tag: String): String = getValue(tag).asSymbol().symbol
    private fun decodeTaggedUndefined(tag: String): BsonUndefined = getValue(tag).let {
        it as? BsonUndefined
            ?: throw BsonInvalidOperationException("Value expected to be of type ${BsonType.UNDEFINED} is of unexpected type ${it.bsonType}")
    }

    private fun decodeTaggedTimestamp(tag: String): BsonTimestamp = getValue(tag).asTimestamp()

}

private class BsonPrimitiveInput(bson: Bson, override val value: BsonValue) : AbstractBsonTreeInput(bson, value) {

    init {
        value.fold(
            primitive = { /* OK */ }
        )
        pushTag(PRIMITIVE_TAG)
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0

    override fun currentElement(tag: String): BsonValue {
        if (tag !== PRIMITIVE_TAG) throw BsonDecodingException("This input can only decode primitives with '$PRIMITIVE_TAG' tag")
        return value
    }

}

private open class BsonTreeInput(bson: Bson, override val value: BsonDocument) : AbstractBsonTreeInput(bson, value) {

    private var position = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (position < descriptor.elementsCount) {
            val name = descriptor.getTag(position++)
            if (name in value) return position - 1
        }
        return CompositeDecoder.READ_DONE
    }

    override fun currentElement(tag: String): BsonValue = value.getValue(tag)

    override fun endStructure(descriptor: SerialDescriptor) {
        if (!bson.configuration.ignoreUnknownKeys) {
            // Validate keys
            val names = descriptor.elementNames()
            for (key in value.keys) {
                if (key !in names) throw BsonDecodingException("Unknown key '$key'")
            }
        }
    }

}

private class BsonTreeMapInput(bson: Bson, override val value: BsonDocument) : BsonTreeInput(bson, value) {

    private val keys = value.keys.toList()
    private val size: Int = keys.size * 2
    private var position = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String {
        return keys[index / 2]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (position < size - 1) return ++position;
        return CompositeDecoder.READ_DONE
    }

    override fun currentElement(tag: String): BsonValue {
        return if (position % 2 == 0) BsonString(tag) else value.getValue(tag)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }

}

private class BsonTreeListInput(bson: Bson, override val value: BsonArray) : AbstractBsonTreeInput(bson, value) {

    private val size = value.size
    private var currentIndex = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String = (index).toString()

    override fun currentElement(tag: String): BsonValue {
        return value[tag.toInt()]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (currentIndex < size - 1) return ++currentIndex
        return CompositeDecoder.READ_DONE
    }

}

internal fun <T> Bson.readBson(
    element: BsonValue,
    deserializer: DeserializationStrategy<T>
): T {
    try {
        val input = element.fold(
            primitive = { BsonPrimitiveInput(this, it) },
            document = { BsonTreeInput(this, it) },
            array = { BsonTreeListInput(this, it) }
        )
        return input.decode(deserializer)
    } catch (ex: BSONException) {
        throw BsonDecodingException(ex.message ?: "", ex)
    }
}