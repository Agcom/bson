# Bson serialization

[Bson](http://bsonspec.org/) serialization format implementation for [Kotlinx serialization](https://github.com/Kotlin/kotlinx.serialization), based on [The BSON library (org.mongodb.bson)](https://mvnrepository.com/artifact/org.mongodb/bson).

> JVM only (#36)

Also, provides useful tools to use with [MongoDB Java driver](https://mongodb.github.io/mongo-java-driver/), e.g. `SerializationCodecRegistry`.

## Setup

Currently, only supports **Kotlin 1.3.72** and **Kotlinx serialization runtime 0.20.0** (#35).

### Gradle

- `*.gradle`

  ```groovy
  repositories {
      jcenter() // Make sure jcenter is added to your project repositories
  }
  
  dependencies {
      implementation 'com.github.agcom:bson-serialization:1.0.1' // The bson serialization library
      implementation 'com.github.agcom:bson-mongodb:1.0.1' // MongoDB driver extensions
  }
  ```

- `*.gradle.kts`: Same as `*.gradle`, with small tweaks.

## Usage

Here is a small example,

```kotlin
import kotlinx.serialization.*
import com.github.agcom.bson.serialization.*
import org.bson.*

@Serializable
data class Project(val name: String, val language: String)

val bson = Bson(BsonConfiguration.DEFAULT)

fun main() {
    val data = Project("com.github.agcom.bson", "Kotlin")
    
    // Serializing
    val bsonValue = bson.toBson(Project.serializer(), data) // A `BsonValue` child, in this case a `BsonDocument`
    println(bsonValue) // {"name": "com.github.agcom.bson", "language": "Kotlin"}
    
    // Deserializing
    println(
        bson.fromBson(Project.serializer(), bsonValue)
    ) // Project(name=com.github.agcom, language=Kotlin)
}
```

### Serialization functions

The following functions can be found in the `com.github.agcom.bson.serialization.Bson` class.

> Doesn't support deprecated bson types (#13).

- `toBson` and `fromBson`: The above example :point_up:

- `dump` and `load`,

  ```kotlin
  import kotlinx.serialization.*
  import com.github.agcom.bson.serialization.*
  import org.bson.*
  
  @Serializable
  data class Project(val name: String, val language: String)
  
  val bson = Bson(BsonConfiguration.DEFAULT)
  
  fun main() {
      val data = Project("com.github.agcom.bson", "Kotlin")
  
      // Dump
      val bytes = bson.dump(Project.serializer(), data)
  
      // Load
      println(
          bson.load(Project.serializer(), bytes)
      ) // Project(name=com.github.agcom, language=Kotlin)
  }
  ```

  > Doesn't support loading/dumping **primitive types**.

#### Serializers

Various **bson types adapter serializers** can be found under `com.github.agcom.bson.serialization.serializers` package.

Those are all registered as default contextual serializers, so you can use `@ContextualSerializer` safely.

> E.g. `BsonValueSerializer`, `TemporalSerializer` and `RegexSerializer`.

### MongoDB driver extensions

Provides extensions to use the serialization library with [MongoDB Java driver](https://mongodb.github.io/mongo-java-driver/).

- Serialization codec

	An adapter between a *serializer* and a `Codec` instance.

	```kotlin
	import kotlinx.serialization.*
	import com.github.agcom.bson.serialization.*
	import org.bson.codecs.Codec
	import com.github.agcom.bson.mongodb.codecs.*
	
	@Serializable
	data class Project(val name: String, val language: String)
	
	val bson = Bson(BsonConfiguration.DEFAULT)
	
	fun main() {
	    val codec: Codec<Project> = SerializationCodec(bson, Project.serializer()) // Look here
	    ...
	}
	```

- Serialization codec registry

  An adapter between a serialization `Bson` instance and `CodecRegistry`.

  ```kotlin
  import kotlinx.serialization.*
  import com.github.agcom.bson.serialization.*
  import com.github.agcom.bson.mongodb.codecs.*
  import org.bson.codecs.configuration.*
  import com.mongodb.MongoClientSettings
  
  @Serializable
  data class Project(val name: String, val language: String)
  
  val bson = Bson(BsonConfiguration.DEFAULT)
  
  fun main() {
      // Composing two registries
  	val registry: CodecRegistry = CodecRegistries.fromRegistries(
          MongoClientSettings.getDefaultCodecRegistry(), // The driver's default codec registry
			SerializationCodecRegistry(bson) // Serialization registry
  	)
      ...
  }
  ```
  
  > It's **recommended** to compose the serialization registry after the **default registry**. This reduces hip-hops (better performance) when working with simple bson types.
  >

