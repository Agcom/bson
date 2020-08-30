package com.github.agcom.bson.serialization.serializers

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.*
import java.time.temporal.Temporal

/**
 * Serializer for [time][java.time] objects which can be represented as epoch millis.
 *
 * The companion object implements this serializer for [Temporal] interface which always deserializes an [Instant] when used.
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 * Uses [DateTimeSerializer].
 */
abstract class TemporalSerializer<T : Temporal> : KSerializer<T> {

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
    companion object : TemporalSerializer<Temporal>() {
        override val descriptor: SerialDescriptor =
            SerialDescriptor(Temporal::class.qualifiedName!!, PolymorphicKind.SEALED)

        override fun toEpochMillis(temporal: Temporal): Long {
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

        override fun ofEpochMillis(temporal: Long): Temporal = Instant.ofEpochMilli(temporal)
    }

    final override fun serialize(encoder: Encoder, value: T) = encoder.encode(DateTimeSerializer, toEpochMillis(value))

    final override fun deserialize(decoder: Decoder): T = ofEpochMillis(decoder.decode(DateTimeSerializer))

    abstract fun toEpochMillis(temporal: T): Long

    abstract fun ofEpochMillis(temporal: Long): T

}

object InstantSerializer : TemporalSerializer<Instant>() {

    override val descriptor: SerialDescriptor = PrimitiveDescriptor(Instant::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun toEpochMillis(temporal: Instant): Long = temporal.toEpochMilli()

    override fun ofEpochMillis(temporal: Long): Instant = Instant.ofEpochMilli(temporal)

}

object LocalDateTimeSerializer : TemporalSerializer<LocalDateTime>() {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(LocalDateTime::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun toEpochMillis(temporal: LocalDateTime): Long =
        InstantSerializer.toEpochMillis(temporal.toInstant(ZoneOffset.UTC))

    override fun ofEpochMillis(temporal: Long): LocalDateTime =
        LocalDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

object LocalDateSerializer : TemporalSerializer<LocalDate>() {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(LocalDate::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun toEpochMillis(temporal: LocalDate): Long =
        LocalDateTimeSerializer.toEpochMillis(temporal.atStartOfDay())

    override fun ofEpochMillis(temporal: Long): LocalDate =
        LocalDateTimeSerializer.ofEpochMillis(temporal).toLocalDate()

}

object LocalTimeSerializer : TemporalSerializer<LocalTime>() {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(LocalTime::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun toEpochMillis(temporal: LocalTime): Long =
        LocalDateTimeSerializer.toEpochMillis(temporal.atDate(LocalDate.now()))

    override fun ofEpochMillis(temporal: Long): LocalTime =
        LocalDateTimeSerializer.ofEpochMillis(temporal).toLocalTime()
}

object OffsetDateTimeSerializer : TemporalSerializer<OffsetDateTime>() {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(OffsetDateTime::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun toEpochMillis(temporal: OffsetDateTime): Long = InstantSerializer.toEpochMillis(temporal.toInstant())

    override fun ofEpochMillis(temporal: Long): OffsetDateTime =
        OffsetDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

object OffsetTimeSerializer : TemporalSerializer<OffsetTime>() {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(OffsetTime::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun toEpochMillis(temporal: OffsetTime): Long =
        OffsetDateTimeSerializer.toEpochMillis(temporal.atDate(LocalDate.now()))

    override fun ofEpochMillis(temporal: Long): OffsetTime =
        OffsetDateTimeSerializer.ofEpochMillis(temporal).toOffsetTime()
}

object ZonedDateTimeSerializer : TemporalSerializer<ZonedDateTime>() {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(ZonedDateTime::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun toEpochMillis(temporal: ZonedDateTime): Long = InstantSerializer.toEpochMillis(temporal.toInstant())

    override fun ofEpochMillis(temporal: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

internal val bsonTemporalModule = SerializersModule {
    contextual(InstantSerializer)
    contextual(LocalDateTimeSerializer)
    contextual(LocalDateSerializer)
    contextual(LocalTimeSerializer)
    contextual(OffsetDateTimeSerializer)
    contextual(OffsetTimeSerializer)
    contextual(ZonedDateTimeSerializer)
    contextual(TemporalSerializer)
}