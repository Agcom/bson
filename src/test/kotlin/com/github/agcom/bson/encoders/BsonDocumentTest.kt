package com.github.agcom.bson.encoders

import com.github.agcom.bson.models.*
import com.github.agom.bson.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.UnitSerializer
import kotlinx.serialization.builtins.serializer
import org.bson.*
import kotlin.random.Random

class BsonDocumentTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "simple" {
        val country = Country("name", "capital")

        bson.toBson(Country.serializer(), country) shouldBe BsonDocument("name", BsonString(country.name)).append(
            "capital",
            BsonString(country.capital)
        )
    }

    "nested" {
        val spot = Spot(
            name = "spot",
            city = City(
                name = "city",
                region = Region(
                    name = "region",
                    capital = "capital",
                    country = Country(
                        name = "country",
                        capital = "capital"
                    )
                ),
                country = Country(
                    name = "country",
                    capital = "capital"
                )
            )
        )
        val expected = BsonDocument().apply {
            putAll(mapOf(
                "name" to BsonString(spot.name),
                "city" to BsonDocument().apply {
                    putAll(mapOf(
                        "name" to BsonString(spot.city.name),
                        "region" to BsonDocument().apply {
                            putAll(mapOf(
                                "name" to BsonString(spot.city.region.name),
                                "capital" to BsonString("capital"),
                                "country" to BsonDocument().apply {
                                    putAll(
                                        mapOf(
                                            "name" to BsonString(spot.city.region.country.name),
                                            "capital" to BsonString((spot.city.region.country.capital))
                                        )
                                    )
                                }
                            ))
                        },
                        "country" to BsonDocument().apply {
                            putAll(
                                mapOf(
                                    "name" to BsonString(spot.city.country.name),
                                    "capital" to BsonString(spot.city.country.capital)
                                )
                            )
                        }
                    ))
                }
            ))
        }
        bson.toBson(Spot.serializer(), spot) shouldBe expected
    }

    "polymorphic" - {

        "sealed" - {

            val tag = Tag("tag")
            val duration = Duration(Random.nextLong())
            val distance = Distance(Random.nextInt())

            "test 1" {
                bson.toBson(Filter.serializer(), tag) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator,
                    BsonString(Tag.serializer().descriptor.serialName)
                ).append("tag", BsonString(tag.tag))
            }
            "test 2" {
                bson.toBson(Filter.serializer(), duration) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator,
                    BsonString(Duration.serializer().descriptor.serialName)
                ).append("duration", BsonInt64(duration.duration))
            }
            "test 3" {
                bson.toBson(Filter.serializer(), distance) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator,
                    BsonString(Distance.serializer().descriptor.serialName)
                ).append("distance", BsonInt32(distance.distance))
            }

        }

        "open" - {

            @Suppress("NAME_SHADOWING")
            val bson = Bson(BsonConfiguration.DEFAULT, animalSerialModule)

            val cat = Cat("Meow")
            val dog = Dog("Hop")

            "test 1" {
                bson.toBson(PolymorphicSerializer(Animal::class), cat) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator, BsonString(Cat.serializer().descriptor.serialName)
                ).append("name", BsonString(cat.name)).append(
                    "sound", BsonString(cat.sound)
                )
            }

            "test 2" {
                bson.toBson(PolymorphicSerializer(Animal::class), dog) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator, BsonString(Dog.serializer().descriptor.serialName)
                ).append("name", BsonString(dog.name)).append(
                    "sound", BsonString(dog.sound)
                )
            }
        }

        "abstract" - {

            @Suppress("NAME_SHADOWING")
            val bson = Bson(BsonConfiguration.DEFAULT, machineSerialModule)

            val car = Car()
            val plane = AirPlane()

            "test 1" {
                bson.toBson(Machine.serializer(), car) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator,
                    BsonString(Car.serializer().descriptor.serialName)
                )
            }

            "test 2" {
                bson.toBson(Machine.serializer(), plane) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator,
                    BsonString(AirPlane.serializer().descriptor.serialName)
                )
            }
        }

        "interface" - {
            @Suppress("NAME_SHADOWING")
            val bson = Bson(BsonConfiguration.DEFAULT, messageSerialModule)

            val msg1 = IntMessage(10)
            val msg2 = StringMessage("hello")

            "test 1" {
                bson.toBson(
                    PolymorphicSerializer(Message::class),
                    msg1
                ) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator,
                    BsonString(IntMessage.serializer().descriptor.serialName)
                ).append("value", BsonInt32(msg1.value))
            }

            "test 2" {
                bson.toBson(
                    PolymorphicSerializer(Message::class),
                    msg2
                ) shouldBe BsonDocument(
                    bson.configuration.classDiscriminator,
                    BsonString(StringMessage.serializer().descriptor.serialName)
                ).append("value", BsonString(msg2.value))
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

})