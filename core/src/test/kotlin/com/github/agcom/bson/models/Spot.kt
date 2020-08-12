package com.github.agcom.bson.models

import kotlinx.serialization.Serializable

@Serializable
data class Spot(
    val name: String,
    val city: City
)

@Serializable
data class City(
    val name: String,
    val region: Region,
    val country: Country
)

@Serializable
data class Region(
    val name: String,
    val capital: String,
    val country: Country
)

@Serializable
data class Country(
    val name: String,
    val capital: String
)