package com.github.agcom.bson.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Filter

@SerialName("tag")
@Serializable
data class Tag(val tag: String) : Filter()

@SerialName("duration")
@Serializable
data class Duration(val duration: Long) : Filter()

@SerialName("distance")
@Serializable
data class Distance(val distance: Int) : Filter()