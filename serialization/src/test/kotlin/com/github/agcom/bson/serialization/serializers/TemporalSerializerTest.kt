package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.BsonInstanceTest
import com.github.agcom.bson.serialization.BsonInstanceTestDefault
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.bson.BsonDateTime
import java.time.*
import java.time.temporal.TemporalAccessor

class TemporalSerializerTest : FreeSpec(), BsonInstanceTest by BsonInstanceTestDefault() {

    private val testInstant = Instant.now()

    private inline infix fun <reified T : TemporalAccessor> TemporalAccessorSerializer<T>.shouldBeOk(testTime: T) {
        val serializer = this
        val testMillis = serializer.toEpochMillis(testTime)
        val encoded = bson.toBson(serializer, testTime)
        encoded.shouldBeInstanceOf<BsonDateTime>(); encoded as BsonDateTime
        encoded.value shouldBe testMillis

        val decoded = bson.fromBson(serializer, encoded)
        serializer.toEpochMillis(decoded) shouldBe testMillis
    }

    init {

        "temporal" {
            TemporalAccessorSerializer shouldBeOk testInstant
        }

        "instant" {
            InstantSerializer shouldBeOk testInstant
        }

        "local date time" {
            LocalDateTimeSerializer shouldBeOk LocalDateTime.ofInstant(testInstant, ZoneId.systemDefault())
        }

        "local date" {
            LocalDateSerializer shouldBeOk LocalDate.ofInstant(testInstant, ZoneId.systemDefault())
        }

        "local time" {
            LocalTimeSerializer shouldBeOk LocalTime.ofInstant(testInstant, ZoneId.systemDefault())
        }

        "offset date time" {
            OffsetDateTimeSerializer shouldBeOk OffsetDateTime.ofInstant(testInstant, ZoneId.systemDefault())
        }

        "offset time" {
            OffsetTimeSerializer shouldBeOk OffsetTime.ofInstant(testInstant, ZoneId.systemDefault())
        }

        "zoned date time" {
            ZonedDateTimeSerializer shouldBeOk ZonedDateTime.ofInstant(testInstant, ZoneId.systemDefault())
        }

    }

}