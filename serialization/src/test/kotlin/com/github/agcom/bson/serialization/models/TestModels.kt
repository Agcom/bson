package com.github.agcom.bson.serialization.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.*
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.time.Clock
import kotlin.random.Random

@Serializable
enum class HttpError {
    NOT_FOUND
}

@Serializable
data class Location(
    val name: String,
    val filters: Set<Filter> = emptySet()
) {

    @Serializable
    sealed class Filter {

        @Serializable
        @SerialName("distance")
        data class Distance(val km: Int) : Filter()

        @Serializable
        @SerialName("duration")
        data class Duration(val ms: Long) : Filter()

    }

}

val testBsonValuePrimitives: List<BsonValue>
    get() = listOf(
        BsonBinary(Random.nextBytes(Random.nextInt(1024, 1048576 + 1))), // [1 Kilobytes, 1 Megabytes]
        BsonBoolean(Random.nextBoolean()),
        BsonDateTime(Clock.systemUTC().millis()),
        BsonDecimal128(Decimal128(Random.nextLong())),
        BsonDouble(Random.nextDouble()),
        BsonInt32(Random.nextInt()),
        BsonInt64(Random.nextLong()),
        BsonJavaScript("main() {}"),
        BsonNull.VALUE,
        BsonObjectId(ObjectId()),
        BsonRegularExpression("hello"),
        BsonRegularExpression("hello", "cdgimstux"),
        BsonString("hello"),
        BsonDbPointer("hello.world", ObjectId()),
        BsonJavaScriptWithScope("main() { console.log(hello) }", BsonDocument("hello", BsonString("bson"))),
        BsonMaxKey(),
        BsonMinKey(),
        BsonSymbol("hello")
    )

fun testBsonDocument(): BsonDocument = BsonDocument().also { doc ->
    // Add primitives
    testBsonValuePrimitives.forEach { bsonValue ->
        doc[bsonValue.bsonType.name] = bsonValue
    }

    // Nested doc
    @Suppress("NAME_SHADOWING")
    doc[BsonType.DOCUMENT.name] = BsonDocument().also { doc ->
        // Primitives
        testBsonValuePrimitives.forEach { bsonValue ->
            doc[bsonValue.bsonType.name] = bsonValue
        }

        // Array
        doc[BsonType.ARRAY.name] = BsonArray().also { array ->
            testBsonValuePrimitives.forEach { bsonValue ->
                array.add(bsonValue)
            }
        }

    }

    // Array
    doc[BsonType.ARRAY.name] = BsonArray().also { array ->
        testBsonValuePrimitives.forEach { bsonValue ->
            array.add(bsonValue)
        }
    }
}

fun testBsonArray(): BsonArray = BsonArray().apply {
    // Add primitives
    testBsonValuePrimitives.forEach { bsonValue ->
        add(bsonValue)
    }

    // Add a document
    val doc = BsonDocument().also { doc ->
        // Add primitives
        testBsonValuePrimitives.forEach { bsonValue ->
            doc[bsonValue.bsonType.name] = bsonValue
        }

        // Nested doc
        @Suppress("NAME_SHADOWING")
        doc[BsonType.DOCUMENT.name] = BsonDocument().also { doc ->
            // Primitives
            testBsonValuePrimitives.forEach { bsonValue ->
                doc[bsonValue.bsonType.name] = bsonValue
            }

            // Array
            doc[BsonType.ARRAY.name] = BsonArray().also { array ->
                testBsonValuePrimitives.forEach { bsonValue ->
                    array.add(bsonValue)
                }
            }

        }

        // Array
        doc[BsonType.ARRAY.name] = BsonArray().also { array ->
            testBsonValuePrimitives.forEach { bsonValue ->
                array.add(bsonValue)
            }
        }
    }
    add(doc)

    // Add an array
    val array = BsonArray().apply {
        testBsonValuePrimitives.forEach { bsonValue ->
            add(bsonValue)
        }
        add(doc)
    }
    add(array)
}