import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
	id("com.gradleup.shadow")
}

dependencies {
	// Common dependencies
	implementation(libs.logback.classic)
	implementation(libs.bundles.jackson)
	testImplementation(libs.kotlin.test.junit)

	// Kafka
	implementation(libs.kafka.clients)

	// Flink Core Bundle
	implementation(libs.bundles.flink.core)

	// Flink Kafka Connector
	implementation(libs.flink.connector.kafka)
}

application {
	mainClass.set("com.example.flink.MainKt")
}

extensions.configure<JavaPluginExtension> {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_11)
	}
}
