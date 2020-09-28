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
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.builtins.*
import org.bson.*
import org.bson.io.BasicOutputBuffer
import org.bson.types.*
import java.time.*
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

class BsonTest : BsonInstanceTest by BsonInstanceTestDefault(), FreeSpec() {

    init {

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

                    "pattern" {
                        val flags =
                            Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS
                        val pattern = Pattern.compile("hello", flags)
                        bson.toBson(PatternSerializer, pattern) shouldBe BsonRegularExpression(
                            pattern.pattern(),
                            "cdgimstux"
                        )
                    }

                    "regex" {
                        val regex = Regex("hello", RegexOption.values().toSet())
                        bson.toBson(RegexSerializer, regex) shouldBe BsonRegularExpression(
                            regex.pattern,
                            "cdimstux"
                        )
                    }

                    "enum kind" {
                        val enum = HttpError.NOT_FOUND
                        bson.toBson(HttpError.serializer(), enum) shouldBe BsonString(enum.name)
                    }

                    "max key" {
                        val maxKey = MaxKey()
                        bson.toBson(MaxKeySerializer, maxKey) shouldBe BsonMaxKey()
                    }

                    "min key" {
                        val minKey = MinKey()
                        bson.toBson(MinKeySerializer, minKey) shouldBe BsonMinKey()
                    }

                    "symbol" {
                        val symbol = "hello"
                        bson.toBson(SymbolSerializer, symbol) shouldBe BsonSymbol("hello")
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

                    "pattern" {
                        val flags =
                            Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS
                        val pattern = Pattern.compile("hello", flags)

                        val test =
                            bson.fromBson(PatternSerializer, BsonRegularExpression(pattern.pattern(), "cdgimstux"))
                        test.pattern() shouldBe pattern.pattern()
                        test.flags() shouldBe pattern.flags()
                    }

                    "regex" {
                        val regex = Regex("hello", RegexOption.values().toSet())

                        val test = bson.fromBson(RegexSerializer, BsonRegularExpression(regex.pattern, "cdimstux"))
                        test.pattern shouldBe regex.pattern
                        test.options shouldBe regex.options
                    }

                    "enum kind" {
                        val enum = HttpError.NOT_FOUND
                        bson.fromBson(HttpError.serializer(), BsonString(enum.name)) shouldBe enum
                    }

                    "max key" {
                        val maxKey = MaxKey()
                        bson.fromBson(MaxKeySerializer, BsonMaxKey()) shouldBe maxKey
                    }

                    "min key" {
                        val minKey = MinKey()
                        bson.fromBson(MinKeySerializer, BsonMinKey()) shouldBe minKey
                    }

                    "symbol" {
                        val symbol = "hello"
                        bson.fromBson(SymbolSerializer, BsonSymbol("hello")) shouldBe symbol
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
                    bson.context.getContextual(BsonBinary::class) shouldBe BsonBinarySerializer
                }

                "bson boolean" {
                    bson.context.getContextual(BsonBoolean::class) shouldBe BsonBooleanSerializer
                }

                "bson date time" {
                    bson.context.getContextual(BsonDateTime::class) shouldBe BsonDateTimeSerializer
                }

                "bson decimal 128" {
                    bson.context.getContextual(BsonDecimal128::class) shouldBe BsonDecimal128Serializer
                }

                "bson double" {
                    bson.context.getContextual(BsonDouble::class) shouldBe BsonDoubleSerializer
                }

                "bson int 32" {
                    bson.context.getContextual(BsonInt32::class) shouldBe BsonInt32Serializer
                }

                "bson int 64" {
                    bson.context.getContextual(BsonInt64::class) shouldBe BsonInt64Serializer
                }

                "bson java script" {
                    bson.context.getContextual(BsonJavaScript::class) shouldBe BsonJavaScriptSerializer
                }

                "bson null" {
                    bson.context.getContextual(BsonNull::class) shouldBe BsonNullSerializer
                }

                "bson number" {
                    bson.context.getContextual(BsonNumber::class) shouldBe BsonNumberSerializer
                }

                "bson object id" {
                    bson.context.getContextual(BsonObjectId::class) shouldBe BsonObjectIdSerializer
                }

                "bson regular expression" {
                    bson.context.getContextual(BsonRegularExpression::class) shouldBe BsonRegularExpressionSerializer
                }

                "bson string" {
                    bson.context.getContextual(BsonString::class) shouldBe BsonStringSerializer
                }

                "bson db pointer" {
                    bson.context.getContextual(BsonDbPointer::class) shouldBe BsonDbPointerSerializer
                }

                "bson java script with scope" {
                    bson.context.getContextual(BsonJavaScriptWithScope::class) shouldBe BsonJavaScriptWithScopeSerializer
                }

                "bson max key" {
                    bson.context.getContextual(BsonMaxKey::class) shouldBe BsonMaxKeySerializer
                }

                "bson min key" {
                    bson.context.getContextual(BsonMinKey::class) shouldBe BsonMinKeySerializer
                }

                "bson symbol" {
                    bson.context.getContextual(BsonSymbol::class) shouldBe BsonSymbolSerializer
                }

                "bson undefined" {
                    bson.context.getContextual(BsonUndefined::class) shouldBe BsonUndefinedSerializer
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

            "max key" {
                bson.context.getContextual(MaxKey::class) shouldBe MaxKeySerializer
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
                    bson.context.getContextual(Temporal::class) shouldBe TemporalAccessorSerializer.Companion
                }

                "temporal accessor" {
                    bson.context.getContextual(TemporalAccessor::class) shouldBe TemporalSerializer
                }

            }

            "code" {
                bson.context.getContextual(Code::class) shouldBe CodeSerializer
            }

            "byte array" {
                bson.context.getContextual(ByteArray::class) shouldBe ByteArraySerializer
            }

            "uuid" {
                bson.context.getContextual(UUID::class).shouldBeInstanceOf<UUIDSerializer>()
            }

            "date" {
                bson.context.getContextual(Date::class) shouldBe DateSerializer
            }

            "min key" {
                bson.context.getContextual(MinKey::class) shouldBe MinKeySerializer
            }

        }

    }

}