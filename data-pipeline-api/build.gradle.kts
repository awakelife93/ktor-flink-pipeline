plugins {
	alias(libs.plugins.ktor)
}

dependencies {
	// Common dependencies
	implementation(libs.logback.classic)
	implementation(libs.bundles.jackson)
	testImplementation(libs.kotlin.test.junit)

	// Kafka
	implementation(libs.kafka.clients)

	// Ktor Server Bundle
	implementation(libs.bundles.ktor.server)

	// Test
	testImplementation(libs.ktor.server.test.host)
}

application {
	mainClass.set("com.example.api.MainKt")
}
