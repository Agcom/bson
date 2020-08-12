package com.github.agcom.bson.decoders

import com.github.agcom.bson.models.*
import com.github.agcom.bson.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.UnitSerializer
import kotlinx.serialization.builtins.serializer
import org.bson.*
import org.bson.io.BasicOutputBuffer
import kotlin.random.Random

class BsonDocumentTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "fromBson" - {

        "simple" {
            val country = Country("name", "capital")
            bson.fromBson(
                Country.serializer(),
                BsonDocument("name", BsonString(country.name)).append("capital", BsonString(country.capital))
            ) shouldBe country
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

            val input = BsonDocument().apply {
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

            bson.fromBson(Spot.serializer(), input) shouldBe spot
        }

        @Suppress("NAME_SHADOWING")
        "polymorphic" - {

            "sealed" - {
                "test 1" {
                    val tag = Tag("tag")
                    bson.fromBson(
                        Filter.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Tag.serializer().descriptor.serialName)
                        ).append("tag", BsonString(tag.tag))
                    ) shouldBe tag
                }

                "test 2" {
                    val duration = Duration(Random.nextLong())
                    bson.fromBson(
                        Filter.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Duration.serializer().descriptor.serialName)
                        ).append("duration", BsonInt64(duration.duration))
                    ) shouldBe duration
                }

                "test 3" {
                    val distance = Distance(Random.nextInt())
                    bson.fromBson(
                        Filter.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Distance.serializer().descriptor.serialName)
                        ).append("distance", BsonInt32(distance.distance))
                    ) shouldBe distance
                }

            }

            "open" - {

                val bson = Bson(BsonConfiguration.DEFAULT, animalSerialModule)

                "test 1" {
                    val cat = Cat("Meow")
                    bson.fromBson(
                        PolymorphicSerializer(Animal::class), BsonDocument(
                            bson.configuration.classDiscriminator, BsonString(Cat.serializer().descriptor.serialName)
                        ).append("name", BsonString(cat.name)).append(
                            "sound", BsonString(cat.sound)
                        )
                    ) shouldBe cat
                }

                "test 2" {
                    val dog = Dog("Hop")
                    bson.fromBson(
                        PolymorphicSerializer(Animal::class), BsonDocument(
                            bson.configuration.classDiscriminator, BsonString(Dog.serializer().descriptor.serialName)
                        ).append("name", BsonString(dog.name)).append(
                            "sound", BsonString(dog.sound)
                        )
                    ) shouldBe dog
                }

            }

            "abstract" - {

                val bson = Bson(BsonConfiguration.DEFAULT, machineSerialModule)

                "test 1" {
                    val car = Car()
                    bson.fromBson(
                        Machine.serializer(),
                        BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Car.serializer().descriptor.serialName)
                        )
                    ) shouldBe car
                }

                "test 2" {
                    val plane = AirPlane()
                    bson.fromBson(
                        Machine.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(AirPlane.serializer().descriptor.serialName)
                        )
                    ) shouldBe plane
                }

            }

            "interface" - {

                val bson = Bson(BsonConfiguration.DEFAULT, messageSerialModule)

                "test 1" {
                    val msg = IntMessage(10)
                    bson.fromBson(
                        PolymorphicSerializer(Message::class), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(IntMessage.serializer().descriptor.serialName)
                        ).append("value", BsonInt32(msg.value))
                    ) shouldBe msg
                }

                "test 2" {
                    val msg = StringMessage("hello")
                    bson.fromBson(
                        PolymorphicSerializer(Message::class), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(StringMessage.serializer().descriptor.serialName)
                        ).append("value", BsonString(msg.value))
                    ) shouldBe msg
                }

            }

        }

        "object" {
            bson.fromBson(UnitSerializer(), BsonDocument()) shouldBe Unit
        }

        "map" {
            val map = mapOf("hello" to "yes", "world" to "yes")
            bson.fromBson(
                MapSerializer(String.serializer(), String.serializer()),
                BsonDocument("hello", BsonString("yes")).append("world", BsonString("yes"))
            ) shouldBe map
        }

    }

    "load" - {

        fun BsonDocument.bytes(): ByteArray {
            val reader = BsonDocumentReader(this)
            return BasicOutputBuffer().use { out ->
                BsonBinaryWriter(out).use {
                    it.pipe(reader)
                }
                out.toByteArray()
            }
        }

        "simple" {
            val country = Country("name", "capital")

            bson.load(
                Country.serializer(), BsonDocument("name", BsonString(country.name)).append(
                    "capital",
                    BsonString(country.capital)
                ).bytes()
            ) shouldBe country
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
            bson.load(Spot.serializer(), expected.bytes()) shouldBe spot
        }

        "polymorphic" - {

            "sealed" - {

                "test 1" {
                    val tag = Tag("tag")
                    bson.load(
                        Filter.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Tag.serializer().descriptor.serialName)
                        ).append("tag", BsonString(tag.tag)).bytes()
                    ) shouldBe tag
                }

                "test 2" {
                    val duration = Duration(Random.nextLong())
                    bson.load(
                        Filter.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Duration.serializer().descriptor.serialName)
                        ).append("duration", BsonInt64(duration.duration)).bytes()
                    ) shouldBe duration
                }

                "test 3" {
                    val distance = Distance(Random.nextInt())
                    bson.load(
                        Filter.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Distance.serializer().descriptor.serialName)
                        ).append("distance", BsonInt32(distance.distance)).bytes()
                    ) shouldBe distance
                }

            }

            "open" - {

                @Suppress("NAME_SHADOWING")
                val bson = Bson(BsonConfiguration.DEFAULT, animalSerialModule)

                "test 1" {
                    val cat = Cat("Meow")
                    bson.load(
                        PolymorphicSerializer(Animal::class), BsonDocument(
                            bson.configuration.classDiscriminator, BsonString(Cat.serializer().descriptor.serialName)
                        ).append("name", BsonString(cat.name)).append(
                            "sound", BsonString(cat.sound)
                        ).bytes()
                    ) shouldBe cat
                }

                "test 2" {
                    val dog = Dog("Hop")
                    bson.load(
                        PolymorphicSerializer(Animal::class), BsonDocument(
                            bson.configuration.classDiscriminator, BsonString(Dog.serializer().descriptor.serialName)
                        ).append("name", BsonString(dog.name)).append(
                            "sound", BsonString(dog.sound)
                        ).bytes()
                    ) shouldBe dog
                }
            }

            "abstract" - {

                @Suppress("NAME_SHADOWING")
                val bson = Bson(BsonConfiguration.DEFAULT, machineSerialModule)

                "test 1" {
                    val car = Car()
                    bson.load(
                        Machine.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(Car.serializer().descriptor.serialName)
                        ).bytes()
                    ) shouldBe car
                }

                "test 2" {
                    val plane = AirPlane()
                    bson.load(
                        Machine.serializer(), BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(AirPlane.serializer().descriptor.serialName)
                        ).bytes()
                    ) shouldBe plane
                }
            }

            "interface" - {
                @Suppress("NAME_SHADOWING")
                val bson = Bson(BsonConfiguration.DEFAULT, messageSerialModule)

                "test 1" {
                    val msg1 = IntMessage(10)
                    bson.load(
                        PolymorphicSerializer(Message::class),
                        BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(IntMessage.serializer().descriptor.serialName)
                        ).append("value", BsonInt32(msg1.value)).bytes()
                    ) shouldBe msg1
                }

                "test 2" {
                    val msg2 = StringMessage("hello")
                    bson.load(
                        PolymorphicSerializer(Message::class),
                        BsonDocument(
                            bson.configuration.classDiscriminator,
                            BsonString(StringMessage.serializer().descriptor.serialName)
                        ).append("value", BsonString(msg2.value)).bytes()
                    ) shouldBe msg2
                }

            }
        }

        "object" {
            bson.load(UnitSerializer(), BsonDocument().bytes()) shouldBe Unit
        }

        "map" {
            val map = mapOf("hello" to "yes", "world" to "yes")

            bson.load(
                MapSerializer(
                    String.serializer(),
                    String.serializer()
                ), BsonDocument("hello", BsonString("yes")).append("world", BsonString("yes")).bytes()
            ) shouldBe map
        }

    }

})