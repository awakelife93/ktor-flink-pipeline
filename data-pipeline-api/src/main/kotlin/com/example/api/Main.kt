package com.example.api

import com.example.api.module.configureContentNegotiation
import com.example.api.module.configureKafkaModule
import com.example.api.module.configureRequestLogging
import com.example.api.module.configureStatusPages
import com.example.api.route.configureRouting
import com.example.api.service.LogService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.EngineMain
import org.slf4j.LoggerFactory

private const val KAFKA_TOPIC = "logs"
private const val KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"

private val logger = LoggerFactory.getLogger("Main")

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.main() {
  configureContentNegotiation()
  configureStatusPages()
  configureRequestLogging()

  runCatching {
    // Configure Kafka Producer
    val kafkaProducer = configureKafkaModule(KAFKA_BOOTSTRAP_SERVERS, KAFKA_TOPIC)
    val logService = LogService(kafkaProducer, KAFKA_TOPIC)

    // Configure routing
    configureRouting(logService)

    // Clean up resources on application shutdown
    monitor.subscribe(ApplicationStopped) {
      logService.close()
    }

    logger.info("Application started successfully")
  }.onFailure { exception ->
    logger.error("Failed to start application", exception)
  }
}
