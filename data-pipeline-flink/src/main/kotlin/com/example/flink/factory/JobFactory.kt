package com.example.flink.factory

import com.example.flink.job.LogProcessorJob
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object JobFactory {
  private val logger: Logger = LoggerFactory.getLogger(this::class.java)

  fun createLogProcessorJob(
    kafkaTopic: String,
    kafkaBootstrapServers: String
  ): () -> Unit {
    logger.info("Creating Log Processor Job with configuration:")
    logger.info("  - Kafka Topic: $kafkaTopic")
    logger.info("  - Kafka Bootstrap Servers: $kafkaBootstrapServers")

    return {
      val job = LogProcessorJob(
        kafkaTopic = kafkaTopic,
        kafkaBootstrapServers = kafkaBootstrapServers
      )
      job.run()
    }
  }
}
