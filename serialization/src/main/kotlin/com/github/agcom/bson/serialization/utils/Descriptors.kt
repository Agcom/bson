package com.github.agcom.bson.serialization.utils

import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialKind
import kotlinx.serialization.StructureKind

internal sealed class ListLikeDescriptor(val elementDesc: SerialDescriptor) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = 1

    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
            name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

    override fun isElementOptional(index: Int): Boolean {
        if (index != 0) throw IllegalStateException("List descriptor has only one child element, index: $index")
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        if (index != 0) throw IndexOutOfBoundsException("List descriptor has only one child element, index: $index")
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor {
        if (index != 0) throw IndexOutOfBoundsException("List descriptor has only one child element, index: $index")
        return elementDesc
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ListLikeDescriptor) return false
        if (elementDesc == other.elementDesc && serialName == other.serialName) return true
        return false
    }

    override fun hashCode(): Int {
        return elementDesc.hashCode() * 31 + serialName.hashCode()
    }
}

internal class NamedListClassDescriptor(override val serialName: String, elementDescriptor: SerialDescriptor) : ListLikeDescriptor(elementDescriptor)

internal sealed class MapLikeDescriptor(
        override val serialName: String,
        val keyDescriptor: SerialDescriptor,
        val valueDescriptor: SerialDescriptor
) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.MAP
    override val elementsCount: Int = 2
    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
            name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid map index")

    override fun isElementOptional(index: Int): Boolean {
        if (index !in 0..1) throw IllegalStateException("Map descriptor has only two child elements, index: $index")
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        if (index !in 0..1) throw IndexOutOfBoundsException("Map descriptor has only two child elements, index: $index")
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor = when (index) {
        0 -> keyDescriptor
        1 -> valueDescriptor
        else -> throw IndexOutOfBoundsException("Map descriptor has only one child element, index: $index")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapLikeDescriptor) return false

        if (serialName != other.serialName) return false
        if (keyDescriptor != other.keyDescriptor) return false
        if (valueDescriptor != other.valueDescriptor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serialName.hashCode()
        result = 31 * result + keyDescriptor.hashCode()
        result = 31 * result + valueDescriptor.hashCode()
        return result
    }
}

internal class NamedMapClassDescriptor(name: String, keyDescriptor: SerialDescriptor, valueDescriptor: SerialDescriptor) : MapLikeDescriptor(name, keyDescriptor, valueDescriptor)