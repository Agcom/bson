package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.BsonInstanceTest
import com.github.agcom.bson.serialization.BsonInstanceTestDefault
import com.github.agcom.bson.serialization.models.testBsonValuePrimitives
import com.github.agcom.bson.serialization.utils.toBinary
import com.github.agcom.bson.serialization.utils.toRegex
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.KSerializer
import org.bson.*

class SpecialSerializersTest : BsonInstanceTest by BsonInstanceTestDefault(), FreeSpec() {

    private fun <T> KSerializer<T>.shouldBeOk(expectedValue: T, expectedBsonValue: BsonValue) {
        val bsonValue = bson.toBson(this, expectedValue)
        bsonValue shouldBe expectedBsonValue
        val value = bson.fromBson(this, expectedBsonValue)
        value shouldBe expectedValue
    }

    init {

        "binary" {
            val bsonValue = testBsonValuePrimitives.filterIsInstance<BsonBinary>().first()
            val binary = bsonValue.toBinary()
            BinarySerializer.shouldBeOk(binary, bsonValue)
        }

        "object id" {
            val bsonValue = testBsonValuePrimitives.filterIsInstance<BsonObjectId>().first()
            val id = bsonValue.value
            ObjectIdSerializer.shouldBeOk(id, bsonValue)
        }

        "date time" {
            val bsonValue = testBsonValuePrimitives.filterIsInstance<BsonDateTime>().first()
            val time = bsonValue.value
            DateTimeSerializer.shouldBeOk(time, bsonValue)
        }

        "java script" {
            val bsonValue = testBsonValuePrimitives.filterIsInstance<BsonJavaScript>().first()
            val js = bsonValue.code
            JavaScriptSerializer.shouldBeOk(js, bsonValue)
        }

        "decimal 128" {
            val bsonValue = testBsonValuePrimitives.filterIsInstance<BsonDecimal128>().first()
            val d = bsonValue.value
            Decimal128Serializer.shouldBeOk(d, bsonValue)
        }

        "regex" {
            val bsonValue = testBsonValuePrimitives.filterIsInstance<BsonRegularExpression>().first()
            val regex = bsonValue.toRegex()
            RegexSerializer.shouldBeOk(regex, bsonValue)
        }

        "pattern" {
            val expectedBsonValue = testBsonValuePrimitives.filterIsInstance<BsonRegularExpression>().last()
            val expectedPattern = expectedBsonValue.toRegex().toPattern()

            val bsonValue = bson.toBson(PatternSerializer, expectedPattern)
            bsonValue shouldBe expectedBsonValue
            val pattern = bson.fromBson(PatternSerializer, expectedBsonValue)
            pattern.pattern() shouldBe expectedPattern.pattern()
            pattern.flags() shouldBe expectedPattern.flags()
        }

    }

}