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
	    jcenter() // Make sure jcenter is added to the repositories
	}
	
	def bson_serialization_version = '1.0.1'
	
	dependencies {
	    implementation "com.github.agcom:bson-serialization:$bson_serialization_version" // The bson serialization library
	    implementation "com.github.agcom:bson-mongodb:$bson_serialization_version" // MongoDB driver extensions; Requires the above dependency
	}
	```

- `*.gradle.kts`: Same as `*.gradle`, with small tweaks.

## Usage

Here is a small example,

> The following example illustrates working with `BsonValue` instances.
>
> Read *[Serialization functions](#serialization-functions)* section below for byte arrays conversations (`dump`, `load`).

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
    val bsonValue = bson.toBson(Project.serializer(), data) // A `BsonValue` child, in this case `BsonDocument`
    println(bsonValue) // {"name": "com.github.agcom.bson", "language": "Kotlin"}
    
    // Deserializing
    println(
        bson.fromBson(Project.serializer(), bsonValue)
    ) // Project(name=com.github.agcom, language=Kotlin)
}
```

### Serialization functions

The following functions can be found in the `com.github.agcom.bson.serialization.Bson` class.

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
	
	    // Load (*1)
	    println(
	        bson.load(Project.serializer(), bytes)
	    ) // Project(name=com.github.agcom, language=Kotlin)
	}
	```

	> 1. Limited functionality (#18). You can use the other signature`load(serializer, bytes, bsonType)` to bypass this issue.

#### Configurations

...

### MongoDB driver extensions

...