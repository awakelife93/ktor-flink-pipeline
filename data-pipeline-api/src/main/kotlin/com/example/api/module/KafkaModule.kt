package com.example.api.module

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import java.util.Properties

private val logger = LoggerFactory.getLogger("KafkaModule")

fun configureKafkaModule(bootstrapServers: String, topicName: String): KafkaProducer<String, String> {
  // Create Kafka topic
  configureKafkaTopic(bootstrapServers, topicName)

  val props = Properties().apply {
    put("bootstrap.servers", bootstrapServers)
    put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  }

  return KafkaProducer<String, String>(props)
}

private fun configureKafkaTopic(bootstrapServers: String, topicName: String) {
  val props = Properties().apply { put("bootstrap.servers", bootstrapServers) }
  val admin = AdminClient.create(props)

  runCatching {
    val existing = admin.listTopics().names().get()

    if (!existing.contains(topicName)) {
      val newTopic = NewTopic(topicName, 1, 1)
      admin.createTopics(listOf(newTopic))
      logger.info("Created Kafka topic: $topicName")
    } else {
      logger.info("Kafka topic already exists: $topicName")
    }
  }.onFailure { exception ->
    logger.error("Failed to manage Kafka topic: $topicName", exception)
  }

  admin.close()
}
