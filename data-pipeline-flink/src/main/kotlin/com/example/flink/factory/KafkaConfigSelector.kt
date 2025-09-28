package com.example.flink.factory

import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("KafkaConfigSelector")

object KafkaConfigSelector {
	val isRunningFromJar = this::class.java.protectionDomain
		.codeSource.location.toString().contains(".jar")

	val kafkaBootstrapServers = if (isRunningFromJar) {
		// shadowJar execution (Flink cluster)
		"ktor-flink-pipeline-kafka:29092"
	} else {
		// IDE execution (local development)
		"localhost:9092"
	}

	init {
		logger.info("KafkaConfigSelector Execution mode - JAR: $isRunningFromJar, Kafka: $kafkaBootstrapServers")
	}
}
