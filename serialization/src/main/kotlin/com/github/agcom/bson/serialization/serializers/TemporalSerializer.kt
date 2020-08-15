package com.github.agcom.bson.serialization.serializers

import kotlinx.serialization.*
import java.time.*
import java.time.temporal.Temporal
import com.github.agcom.bson.serialization.encoders.BsonOutput

/**
 * Serializer for time objects which can be represented as epoch millis.
 * Uses [BsonOutput.encodeDateTime].
 */
@Serializer(Temporal::class)
abstract class TemporalSerializer<T : Temporal>(serialName: String) : KSerializer<T> {

    companion object : TemporalSerializer<Temporal>(Temporal::class.qualifiedName!!) {
        override fun toEpochMillis(temporal: Temporal): Long {
            return when(temporal) {
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

    final override val descriptor: SerialDescriptor = PrimitiveDescriptor(serialName, PrimitiveKind.LONG)

    final override fun serialize(encoder: Encoder, value: T) = encoder.encode(DateTimeSerializer, toEpochMillis(value))

    final override fun deserialize(decoder: Decoder): T = ofEpochMillis(decoder.decode(DateTimeSerializer))

    abstract fun toEpochMillis(temporal: T): Long

    abstract fun ofEpochMillis(temporal: Long): T

}

//@Serializer(Instant::class) // Should not activate this, or will get weird exceptions like java.lang.NoSuchMethodError: java.time.Instant: method 'void <init>()' not found
object InstantSerializer : TemporalSerializer<Instant>(Instant::class.qualifiedName!!) {

    override fun toEpochMillis(temporal: Instant): Long = temporal.toEpochMilli()

    override fun ofEpochMillis(temporal: Long): Instant = Instant.ofEpochMilli(temporal)

}

//@Serializer(LocalDateTime::class)
object LocalDateTimeSerializer : TemporalSerializer<LocalDateTime>(LocalDateTime::class.qualifiedName!!) {

    override fun toEpochMillis(temporal: LocalDateTime): Long =
        InstantSerializer.toEpochMillis(temporal.toInstant(ZoneOffset.UTC))

    override fun ofEpochMillis(temporal: Long): LocalDateTime =
        LocalDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

//@Serializer(LocalDate::class)
object LocalDateSerializer : TemporalSerializer<LocalDate>(LocalDate::class.qualifiedName!!) {

    override fun toEpochMillis(temporal: LocalDate): Long = LocalDateTimeSerializer.toEpochMillis(temporal.atStartOfDay())

    override fun ofEpochMillis(temporal: Long): LocalDate =
        LocalDateTimeSerializer.ofEpochMillis(temporal).toLocalDate()

}

//@Serializer(LocalTime::class)
object LocalTimeSerializer : TemporalSerializer<LocalTime>(LocalTime::class.qualifiedName!!) {

    override fun toEpochMillis(temporal: LocalTime): Long =
        LocalDateTimeSerializer.toEpochMillis(temporal.atDate(LocalDate.now()))

    override fun ofEpochMillis(temporal: Long): LocalTime =
        LocalDateTimeSerializer.ofEpochMillis(temporal).toLocalTime()
}

//@Serializer(OffsetDateTime::class)
object OffsetDateTimeSerializer : TemporalSerializer<OffsetDateTime>(OffsetDateTime::class.qualifiedName!!) {

    override fun toEpochMillis(temporal: OffsetDateTime): Long = InstantSerializer.toEpochMillis(temporal.toInstant())

    override fun ofEpochMillis(temporal: Long): OffsetDateTime =
        OffsetDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}

//@Serializer(OffsetTime::class)
object OffsetTimeSerializer : TemporalSerializer<OffsetTime>(OffsetTime::class.qualifiedName!!) {

    override fun toEpochMillis(temporal: OffsetTime): Long =
        OffsetDateTimeSerializer.toEpochMillis(temporal.atDate(LocalDate.now()))

    override fun ofEpochMillis(temporal: Long): OffsetTime =
        OffsetDateTimeSerializer.ofEpochMillis(temporal).toOffsetTime()
}

//@Serializer(ZonedDateTime::class)
object ZonedDateTimeSerializer : TemporalSerializer<ZonedDateTime>(ZonedDateTime::class.qualifiedName!!) {

    override fun toEpochMillis(temporal: ZonedDateTime): Long = InstantSerializer.toEpochMillis(temporal.toInstant())

    override fun ofEpochMillis(temporal: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(InstantSerializer.ofEpochMillis(temporal), ZoneOffset.UTC)

}