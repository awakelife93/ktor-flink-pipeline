plugins {
	kotlin("jvm") version "2.2.20" apply false
	id("io.ktor.plugin") version "3.3.0" apply false
	id("com.gradleup.shadow") version "9.1.0" apply false
}

group = "com.example"
version = "0.0.1"

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
}
