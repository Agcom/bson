package com.github.agcom.bson.serialization

import com.github.agcom.bson.serialization.models.*
import com.github.agcom.bson.serialization.serializers.*
import com.github.agcom.bson.serialization.utils.toBsonBinary
import com.github.agcom.bson.serialization.utils.writeBsonArray
import com.github.agcom.bson.serialization.utils.writeBsonDocument
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.*
import org.bson.*
import org.bson.io.BasicOutputBuffer
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

class BsonTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "to bson" - {

        "primitives" - {

            "bson primitive types" {

                forAll(*testBsonValuePrimitives.map { row(it) }.toTypedArray()) { bsonValue ->
                    bson.toBson(BsonValueSerializer, bsonValue) shouldBe bsonValue
                }

            }

            "platform types" - {

                "double" {
                    val d = Random.nextDouble()
                    bson.toBson(Double.serializer(), d) shouldBe BsonDouble(d)
                }

                "string" {
                    val str = UUID.randomUUID().toString()
                    bson.toBson(String.serializer(), str) shouldBe BsonString(str)
                }

                "binary" {
                    val binary =
                        Binary(Random.nextBytes(Random.nextInt(1024, 1048576 + 1))) // [1 Kilobytes, 1 Megabytes]
                    bson.toBson(BinarySerializer, binary) shouldBe binary.toBsonBinary()
                }

                "object id" {
                    val id = ObjectId()
                    bson.toBson(ObjectIdSerializer, id) shouldBe BsonObjectId(id)
                }

                "boolean" {
                    val bool = Random.nextBoolean()
                    bson.toBson(Boolean.serializer(), bool) shouldBe BsonBoolean(bool)
                }

                "date time" {
                    val t = Clock.systemUTC().millis()
                    bson.toBson(DateTimeSerializer, t) shouldBe BsonDateTime(t)
                }

                "null" {
                    bson.toBson(String.serializer().nullable, null) shouldBe BsonNull.VALUE
                }

                "java script" {
                    val code = "main() {}"
                    bson.toBson(JavaScriptSerializer, code) shouldBe BsonJavaScript(code)
                }

                "int 32" {
                    val i = Random.nextInt()
                    bson.toBson(Int.serializer(), i) shouldBe BsonInt32(i)
                }

                "int 64" {
                    val l = Random.nextLong()
                    bson.toBson(Long.serializer(), l) shouldBe BsonInt64(l)
                }

                "decimal 128" {
                    val decimal = Decimal128.fromIEEE754BIDEncoding(Random.nextLong(), Random.nextLong())
                    bson.toBson(Decimal128Serializer, decimal) shouldBe BsonDecimal128(decimal)
                }

                "regular expresion" - {

                    "without options" {
                        val regex = Regex("acme.*corp")
                        bson.toBson(RegexSerializer, regex) shouldBe BsonRegularExpression(regex.pattern)
                    }

                    "with options" {
                        val regex =
                            Regex(
                                "acme.*corp", setOf(
                                    RegexOption.IGNORE_CASE,
                                    RegexOption.MULTILINE,
                                    RegexOption.LITERAL,
                                    RegexOption.UNIX_LINES,
                                    RegexOption.COMMENTS,
                                    RegexOption.DOT_MATCHES_ALL,
                                    RegexOption.CANON_EQ
                                )
                            )
                        bson.toBson(RegexSerializer, regex) shouldBe BsonRegularExpression(regex.pattern, "imdxs")
                    }
                }

                "enum kind" {
                    val enum = HttpError.NOT_FOUND
                    bson.toBson(HttpError.serializer(), enum) shouldBe BsonString(enum.name)
                }

            }

        }

        "document" - {

            "bson document type" {
                val doc = testBsonDocument()

                bson.toBson(BsonDocumentSerializer, doc) shouldBe doc
                bson.toBson(BsonValueSerializer, doc) shouldBe doc
            }

            "platform types" - {

                "a class" {
                    val location = Location(
                        name = "name",
                        filters = setOf(
                            Location.Filter.Distance(km = 10),
                            Location.Filter.Duration(ms = 100000)
                        )
                    )

                    bson.toBson(Location.serializer(), location) shouldBe BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    }
                }

                "object" {
                    bson.toBson(UnitSerializer(), Unit) shouldBe BsonDocument()
                }

                "map" {
                    val map = mapOf("hello" to "yes", "world" to "yes")

                    bson.toBson(
                        MapSerializer(
                            String.serializer(),
                            String.serializer()
                        ), map
                    ) shouldBe BsonDocument("hello", BsonString("yes")).append("world", BsonString("yes"))
                }

            }

        }

        "array" - {

            "bson array type" {
                val array = testBsonArray()

                bson.toBson(BsonArraySerializer, array) shouldBe array
                bson.toBson(BsonValueSerializer, array) shouldBe array
            }

            "platform type" {
                val location = Location(
                    name = "name",
                    filters = setOf(
                        Location.Filter.Distance(km = 10),
                        Location.Filter.Duration(ms = 100000)
                    )
                )
                val list = listOf(location, location)

                bson.toBson(Location.serializer().list, list) shouldBe BsonArray().apply {
                    add(BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    })
                    add(get(0))
                }
            }

        }

    }

    "from bson" - {

        "primitives" - {

            "bson primitive types" {

                forAll(*testBsonValuePrimitives.map { row(it) }.toTypedArray()) { bsonValue ->
                    bson.fromBson(BsonValueSerializer, bsonValue) shouldBe bsonValue
                }

            }

            "platform types" - {

                "double" {
                    val d = Random.nextDouble()
                    bson.fromBson(Double.serializer(), BsonDouble(d)) shouldBe d
                }

                "string" {
                    val str = UUID.randomUUID().toString()
                    bson.fromBson(String.serializer(), BsonString(str)) shouldBe str
                }

                "binary" {
                    val binary =
                        Binary(Random.nextBytes(Random.nextInt(1024, 1048576 + 1))) // [1 Kilobytes, 1 Megabytes]
                    bson.fromBson(BinarySerializer, binary.toBsonBinary()) shouldBe binary
                }

                "object id" {
                    val id = ObjectId()
                    bson.fromBson(ObjectIdSerializer, BsonObjectId(id)) shouldBe id
                }

                "boolean" {
                    val bool = Random.nextBoolean()
                    bson.fromBson(Boolean.serializer(), BsonBoolean(bool)) shouldBe bool
                }

                "date time" {
                    val t = Clock.systemUTC().millis()
                    bson.fromBson(DateTimeSerializer, BsonDateTime(t)) shouldBe t
                }

                "null" {
                    bson.fromBson(UnitSerializer().nullable, BsonNull.VALUE) shouldBe null
                }

                "java script" {
                    val code = "main() {}"
                    bson.fromBson(JavaScriptSerializer, BsonJavaScript(code)) shouldBe code
                }

                "int 32" {
                    val i = Random.nextInt()
                    bson.fromBson(Int.serializer(), BsonInt32(i)) shouldBe i
                }

                "int 64" {
                    val l = Random.nextLong()
                    bson.fromBson(Long.serializer(), BsonInt64(l)) shouldBe l
                }

                "decimal 128" {
                    val decimal = Decimal128.fromIEEE754BIDEncoding(Random.nextLong(), Random.nextLong())
                    bson.fromBson(Decimal128Serializer, BsonDecimal128(decimal)) shouldBe decimal
                }

                "regular expresion" - {

                    "without options" {
                        val regex = Regex("acme.*corp")
                        bson.fromBson(RegexSerializer, BsonRegularExpression(regex.pattern)) shouldBe regex
                    }

                    "with options" {
                        val regex =
                            Regex(
                                "acme.*corp", setOf(
                                    RegexOption.IGNORE_CASE,
                                    RegexOption.MULTILINE,
                                    RegexOption.UNIX_LINES,
                                    RegexOption.COMMENTS,
                                    RegexOption.DOT_MATCHES_ALL
                                )
                            )
                        bson.fromBson(RegexSerializer, BsonRegularExpression(regex.pattern, "imdxs")) shouldBe regex
                    }
                }

                "enum kind" {
                    val enum = HttpError.NOT_FOUND
                    bson.fromBson(HttpError.serializer(), BsonString(enum.name)) shouldBe enum
                }

            }

        }

        "document" - {

            "bson document type" {
                val doc = testBsonDocument()

                bson.fromBson(BsonDocumentSerializer, doc) shouldBe doc
                bson.fromBson(BsonValueSerializer, doc) shouldBe doc
            }

            "platform types" - {

                "a class" {
                    val location = Location(
                        name = "name",
                        filters = setOf(
                            Location.Filter.Distance(km = 10),
                            Location.Filter.Duration(ms = 100000)
                        )
                    )
                    val doc = BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    }

                    bson.fromBson(Location.serializer(), doc) shouldBe location
                }

                "object" {
                    bson.fromBson(UnitSerializer(), BsonDocument()) shouldBe Unit
                }

                "map" {
                    val map = mapOf("hello" to "yes", "world" to "yes")
                    val doc = BsonDocument("hello", BsonString("yes")).append("world", BsonString("yes"))

                    bson.fromBson(
                        MapSerializer(
                            String.serializer(),
                            String.serializer()
                        ), doc
                    ) shouldBe map
                }

            }

        }

        "array" - {

            "bson array type" {
                val array = testBsonArray()

                bson.fromBson(BsonArraySerializer, array) shouldBe array
                bson.fromBson(BsonValueSerializer, array) shouldBe array
            }

            "platform type" {
                val location = Location(
                    name = "name",
                    filters = setOf(
                        Location.Filter.Distance(km = 10),
                        Location.Filter.Duration(ms = 100000)
                    )
                )
                val list = listOf(location, location)
                val arr = BsonArray().apply {
                    add(BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dur ->
                                dur["type"] = BsonString("duration")
                                dur["ms"] = BsonInt64(100000)
                            })
                        }
                    })
                    add(get(0).asDocument().clone())
                }

                bson.fromBson(Location.serializer().list, arr) shouldBe list
            }

        }

    }

    "load bson document" {
        val doc = testBsonDocument()
        val bytes = BasicOutputBuffer().use {
            it.writeBsonDocument(doc)
            it.toByteArray()
        }
        bson.loadBsonDocument(bytes) shouldBe doc
    }

    "load bson array" {
        val arr = testBsonArray()
        val bytes = BasicOutputBuffer().use {
            it.writeBsonArray(arr)
            it.toByteArray()
        }
        bson.loadBsonArray(bytes) shouldBe arr
    }

    "dump bson" - {

        "document" {
            val doc = testBsonDocument()
            val bytes = BasicOutputBuffer().use {
                it.writeBsonDocument(doc)
                it.toByteArray()
            }
            bson.dumpBson(doc) shouldBe bytes
        }

        "array" {
            val arr = testBsonArray()
            val bytes = BasicOutputBuffer().use {
                it.writeBsonArray(arr)
                it.toByteArray()
            }
            bson.dumpBson(arr) shouldBe bytes
        }

    }

    "dump" - {

        "document" - {

            "bson document type" {
                val doc = testBsonDocument()
                val bytes = BasicOutputBuffer().use {
                    it.writeBsonDocument(doc)
                    it.toByteArray()
                }

                bson.dump(BsonDocumentSerializer, doc) shouldBe bytes
                bson.dump(BsonValueSerializer, doc) shouldBe bytes
            }

            "platform types" - {

                "a class" {
                    val location = Location(
                        name = "name",
                        filters = setOf(
                            Location.Filter.Distance(km = 10),
                            Location.Filter.Duration(ms = 100000)
                        )
                    )
                    val doc = BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    }
                    val bytes = BasicOutputBuffer().use {
                        it.writeBsonDocument(doc)
                        it.toByteArray()
                    }

                    bson.dump(Location.serializer(), location) shouldBe bytes
                }

                "object" {
                    bson.dump(UnitSerializer(), Unit) shouldBe byteArrayOf(
                        5, 0, 0, 0,
                        0
                    )
                }

                "map" {
                    val map = mapOf("hello" to "yes", "world" to "yes")
                    val doc = BsonDocument("hello", BsonString("yes")).append("world", BsonString("yes"))
                    val bytes = BasicOutputBuffer().use {
                        it.writeBsonDocument(doc)
                        it.toByteArray()
                    }

                    bson.dump(
                        MapSerializer(
                            String.serializer(),
                            String.serializer()
                        ), map
                    ) shouldBe bytes
                }

            }

        }

        "array" - {

            "bson array type" {
                val arr = testBsonArray()
                val bytes = BasicOutputBuffer().use {
                    it.writeBsonArray(arr)
                    it.toByteArray()
                }

                bson.dump(BsonArraySerializer, arr) shouldBe bytes
                bson.dump(BsonValueSerializer, arr) shouldBe bytes
            }

            "platform type" {
                val location = Location(
                    name = "name",
                    filters = setOf(
                        Location.Filter.Distance(km = 10),
                        Location.Filter.Duration(ms = 100000)
                    )
                )
                val list = listOf(location, location)
                val arr = BsonArray().apply {
                    add(BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    })
                    add(get(0))
                }
                val bytes = BasicOutputBuffer().use {
                    it.writeBsonArray(arr)
                    it.toByteArray()
                }

                bson.dump(Location.serializer().list, list) shouldBe bytes
            }

        }

    }

    "load" - {

        "document" - {

            "bson document type" {
                val doc = testBsonDocument()
                val bytes = BasicOutputBuffer().use {
                    it.writeBsonDocument(doc)
                    it.toByteArray()
                }

                bson.load(BsonDocumentSerializer, bytes) shouldBe doc
                bson.load(BsonValueSerializer, bytes) shouldBe doc
            }

            "platform types" - {

                "a class" {
                    val location = Location(
                        name = "name",
                        filters = setOf(
                            Location.Filter.Distance(km = 10),
                            Location.Filter.Duration(ms = 100000)
                        )
                    )
                    val doc = BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    }
                    val bytes = BasicOutputBuffer().use {
                        it.writeBsonDocument(doc)
                        it.toByteArray()
                    }

                    bson.load(Location.serializer(), bytes) shouldBe location
                }

                "object" {
                    bson.load(
                        UnitSerializer(), byteArrayOf(5, 0, 0, 0, 0)
                    ) shouldBe Unit
                }

                "map" {
                    val map = mapOf("hello" to "yes", "world" to "yes")
                    val doc = BsonDocument("hello", BsonString("yes")).append("world", BsonString("yes"))
                    val bytes = BasicOutputBuffer().use {
                        it.writeBsonDocument(doc)
                        it.toByteArray()
                    }

                    bson.load(
                        MapSerializer(
                            String.serializer(),
                            String.serializer()
                        ), bytes
                    ) shouldBe map
                }

            }

        }

        "array" - {

            "bson array type" {
                val arr = testBsonArray()
                val bytes = BasicOutputBuffer().use {
                    it.writeBsonArray(arr)
                    it.toByteArray()
                }

                bson.dump(BsonArraySerializer, arr) shouldBe bytes
                bson.dump(BsonValueSerializer, arr) shouldBe bytes
            }

            "platform type" {
                val location = Location(
                    name = "name",
                    filters = setOf(
                        Location.Filter.Distance(km = 10),
                        Location.Filter.Duration(ms = 100000)
                    )
                )
                val list = listOf(location, location)
                val arr = BsonArray().apply {
                    add(BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    })
                    add(get(0))
                }
                val bytes = BasicOutputBuffer().use {
                    it.writeBsonArray(arr)
                    it.toByteArray()
                }

                bson.dump(Location.serializer().list, list) shouldBe bytes
            }

        }

    }

    "default context" - {

        "bson value" {
            bson.context.getContextual(BsonValue::class) shouldBe BsonValueSerializer
        }

        "bson document" {
            bson.context.getContextual(BsonDocument::class) shouldBe BsonDocumentSerializer
        }

        "bson array" {
            bson.context.getContextual(BsonArray::class) shouldBe BsonArraySerializer
        }

        "bson primitives" - {

            "bson binary" {
                bson.context.getContextual(BsonBinary::class) shouldBe BsonPrimitiveSerializer
            }

            "bson boolean" {
                bson.context.getContextual(BsonBoolean::class) shouldBe BsonPrimitiveSerializer
            }

            "bson date time" {
                bson.context.getContextual(BsonDateTime::class) shouldBe BsonPrimitiveSerializer
            }

            "bson decimal 128" {
                bson.context.getContextual(BsonDecimal128::class) shouldBe BsonPrimitiveSerializer
            }

            "bson double" {
                bson.context.getContextual(BsonDouble::class) shouldBe BsonPrimitiveSerializer
            }

            "bson int 32" {
                bson.context.getContextual(BsonInt32::class) shouldBe BsonPrimitiveSerializer
            }

            "bson int 64" {
                bson.context.getContextual(BsonInt64::class) shouldBe BsonPrimitiveSerializer
            }

            "bson java script" {
                bson.context.getContextual(BsonJavaScript::class) shouldBe BsonPrimitiveSerializer
            }

            "bson null" {
                bson.context.getContextual(BsonNull::class) shouldBe BsonPrimitiveSerializer
            }

            "bson number" {
                bson.context.getContextual(BsonNumber::class) shouldBe BsonPrimitiveSerializer
            }

            "bson object id" {
                bson.context.getContextual(BsonObjectId::class) shouldBe BsonPrimitiveSerializer
            }

            "bson regular expression" {
                bson.context.getContextual(BsonRegularExpression::class) shouldBe BsonPrimitiveSerializer
            }

            "bson string" {
                bson.context.getContextual(BsonString::class) shouldBe BsonPrimitiveSerializer
            }

        }

        "binary" {
            bson.context.getContextual(Binary::class) shouldBe BinarySerializer
        }

        "object id" {
            bson.context.getContextual(ObjectId::class) shouldBe ObjectIdSerializer
        }

        "decimal 128" {
            bson.context.getContextual(Decimal128::class) shouldBe Decimal128Serializer
        }

        "regex" {
            bson.context.getContextual(Regex::class) shouldBe RegexSerializer
        }

        "pattern" {
            bson.context.getContextual(Pattern::class) shouldBe PatternSerializer
        }

        "temporals" - {

            "instant" {
                bson.context.getContextual(Instant::class) shouldBe InstantSerializer
            }

            "local date time" {
                bson.context.getContextual(LocalDateTime::class) shouldBe LocalDateTimeSerializer
            }

            "local date" {
                bson.context.getContextual(LocalDate::class) shouldBe LocalDateSerializer
            }

            "local time" {
                bson.context.getContextual(LocalTime::class) shouldBe LocalTimeSerializer
            }

            "offset date time" {
                bson.context.getContextual(OffsetDateTime::class) shouldBe OffsetDateTimeSerializer
            }

            "offset time" {
                bson.context.getContextual(OffsetTime::class) shouldBe OffsetTimeSerializer
            }

            "zoned date time" {
                bson.context.getContextual(ZonedDateTime::class) shouldBe ZonedDateTimeSerializer
            }

            "temporal" {
                bson.context.getContextual(Temporal::class) shouldBe TemporalSerializer.Companion
            }

        }

    }

})