package com.github.agcom.bson.serialization.serializers

import kotlinx.serialization.*
import kotlinx.serialization.modules.serializersModuleOf
import java.time.*
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor

/**
 * Serializer for [time][java.time] objects which can be represented as epoch millis.
 *
 * The companion object implements this serializer for [TemporalAccessor] interface which always deserializes an [Instant] when used.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [DateTimeSerializer].
 */
abstract class TemporalAccessorSerializer<T : TemporalAccessor> : KSerializer<T> {

    /**
     * The parent class [Temporal] serializer.
     *
     * Ports to a child serializer when serializing, and always deserializes as [Instant] class.
     *
     * Supports serializing:
     * - [Instant]
     * - [LocalDateTime]
     * - [LocalDate]
     * - [LocalTime]
     * - [OffsetTime]
     * - [ZonedDateTime]
     */
    companion object : TemporalAccessorSerializer<TemporalAccessor>() {

        override fun toEpochMillis(temporal: TemporalAccessor): Long {
            return when (temporal) {
                is Instant -> InstantSerializer.toEpochMillis(temporal)
                is LocalDateTime -> LocalDateTimeSerializer.toEpochMillis(temporal)
                is LocalDate -> LocalDateSerializer.toEpochMillis(temporal)
                is LocalTime -> LocalTimeSerializer.toEpochMillis(temporal)
                is OffsetTime -> OffsetTimeSerializer.toEpochMillis(temporal)
                is ZonedDateTime -> ZonedDateTimeSerializer.toEpochMillis(temporal)
                else -> throw IllegalArgumentException("Unsupported temporal class '${temporal::class.java.name}'")
            }
        }

        override fun ofEpochMillis(temporal: Long): Instant = Instant.ofEpochMilli(temporal)
    }

    final override val descriptor: SerialDescriptor = DateTimeSerializer.descriptor

    final override fun serialize(encoder: Encoder, value: T) = encoder.encode(DateTimeSerializer, toEpochMillis(value))

    final override fun deserialize(decoder: Decoder): T = ofEpochMillis(decoder.decode(DateTimeSerializer))

    abstract fun toEpochMillis(temporal: T): Long

    abstract fun ofEpochMillis(temporal: Long): T

}

typealias TemporalSerializer = TemporalAccessorSerializer.Companion

object InstantSerializer : TemporalAccessorSerializer<Instant>() {

    override fun toEpochMillis(temporal: Instant): Long = temporal.toEpochMilli()

    override fun ofEpochMillis(temporal: Long): Instant = Instant.ofEpochMilli(temporal)

}

object LocalDateTimeSerializer : TemporalAccessorSerializer<LocalDateTime>() {

    override fun toEpochMillis(temporal: LocalDateTime): Long =
        InstantSerializer.toEpochMillis(temporal.toInstant(ZoneOffset.UTC))

    override fun ofEpochMillis(temporal: Long): LocalDateTime =
        LocalDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

object LocalDateSerializer : TemporalAccessorSerializer<LocalDate>() {

    override fun toEpochMillis(temporal: LocalDate): Long =
        LocalDateTimeSerializer.toEpochMillis(temporal.atStartOfDay())

    override fun ofEpochMillis(temporal: Long): LocalDate =
        LocalDateTimeSerializer.ofEpochMillis(temporal).toLocalDate()

}

object LocalTimeSerializer : TemporalAccessorSerializer<LocalTime>() {

    override fun toEpochMillis(temporal: LocalTime): Long =
        LocalDateTimeSerializer.toEpochMillis(temporal.atDate(LocalDate.now()))

    override fun ofEpochMillis(temporal: Long): LocalTime =
        LocalDateTimeSerializer.ofEpochMillis(temporal).toLocalTime()
}

object OffsetDateTimeSerializer : TemporalAccessorSerializer<OffsetDateTime>() {

    override fun toEpochMillis(temporal: OffsetDateTime): Long = InstantSerializer.toEpochMillis(temporal.toInstant())

    override fun ofEpochMillis(temporal: Long): OffsetDateTime =
        OffsetDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

object OffsetTimeSerializer : TemporalAccessorSerializer<OffsetTime>() {

    override fun toEpochMillis(temporal: OffsetTime): Long =
        OffsetDateTimeSerializer.toEpochMillis(temporal.atDate(LocalDate.now()))

    override fun ofEpochMillis(temporal: Long): OffsetTime =
        OffsetDateTimeSerializer.ofEpochMillis(temporal).toOffsetTime()
}

object ZonedDateTimeSerializer : TemporalAccessorSerializer<ZonedDateTime>() {

    override fun toEpochMillis(temporal: ZonedDateTime): Long = InstantSerializer.toEpochMillis(temporal.toInstant())

    override fun ofEpochMillis(temporal: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

internal val bsonTemporalModule = serializersModuleOf(
    mapOf(
        Instant::class to InstantSerializer,
        LocalDateTime::class to LocalDateTimeSerializer,
        LocalDate::class to LocalDateSerializer,
        LocalTime::class to LocalTimeSerializer,
        OffsetDateTime::class to OffsetDateTimeSerializer,
        OffsetTime::class to OffsetTimeSerializer,
        ZonedDateTime::class to ZonedDateTimeSerializer,
        TemporalAccessor::class to TemporalAccessorSerializer,
        Temporal::class to TemporalSerializer
    )
)