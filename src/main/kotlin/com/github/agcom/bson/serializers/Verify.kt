package com.github.agcom.bson.serializers

import com.github.agcom.bson.decoders.BsonInput
import com.github.agcom.bson.encoders.BsonOutput
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder

internal fun Decoder.verify() {
    if (this !is BsonInput) throw IllegalStateException(
            "This serializer can be used only with Bson format." +
                    "Expected Decoder to be BsonInput, got ${this::class}"
    )
}

internal fun Encoder.verify() {
    if (this !is BsonOutput) throw IllegalStateException(
            "This serializer can be used only with Bson format." +
                    "Expected Encoder to be BsonOutput, got ${this::class}"
    )
}