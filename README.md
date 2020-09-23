# Bson serialization

[Bson](http://bsonspec.org/) serialization format implementation for [Kotlinx serialization](https://github.com/Kotlin/kotlinx.serialization), based on [The BSON library (org.mongodb.bson)](https://mvnrepository.com/artifact/org.mongodb/bson).

> JVM only.

Also, provides useful tools to integrate with [MongoDB Java driver](https://mongodb.github.io/mongo-java-driver/). For example, the out of box `SerializationCodecRegistry`.

## Setup

Currently, only supports **Kotlinx serialization runtime 0.20.0**.

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

- `*.gradle.kts`: Same as `*.gradle`, with some small tweaks.

## Usage

Here is a small example,

```kotlin
import kotlinx.serialization.*
import com.github.agcom.bson.serialization.*
import org.bson.*

@Serializable
data class Project(val name: String, val language: String)

val bson = Bson()

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

> Doesn't support deprecated bson types.

- `toBson` and `fromBson`: The above example :point_up:.

- `dump` and `load`:

  ```kotlin
  import kotlinx.serialization.*
  import com.github.agcom.bson.serialization.*
  import org.bson.*
  
  @Serializable
  data class Project(val name: String, val language: String)
  
  val bson = Bson()
  
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

> For example, `BsonValueSerializer`, `TemporalSerializer` and `RegexSerializer`.

### MongoDB driver extensions

Provides extensions to integrate with [MongoDB Java driver](https://mongodb.github.io/mongo-java-driver/).

- Serialization codec

	An adapter between `KSerializer` and `Codec`.

	```kotlin
	import kotlinx.serialization.*
	import com.github.agcom.bson.serialization.*
	import org.bson.codecs.Codec
	import com.github.agcom.bson.mongodb.codecs.*
	
	@Serializable
	data class Project(val name: String, val language: String)
	
	val bson = Bson()
	
	fun main() {
	    val codec: Codec<Project> serializer= SerializationCodec(bson, Project.serializer()) // Look here
	    ...
	}
	```

- Serialization codec registry

  An adapter between `Bson` and `CodecRegistry`.

  ```kotlin
  import kotlinx.serialization.*
  import com.github.agcom.bson.serialization.*
  import com.github.agcom.bson.mongodb.codecs.*
  import org.bson.codecs.configuration.*
  import com.mongodb.MongoClientSettings
  
  @Serializable
  data class Project(val name: String, val language: String)
  
  val bson = Bson()
  
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
