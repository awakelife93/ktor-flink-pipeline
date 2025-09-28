package com.example.api.service

import com.example.api.dto.LogEvent
import com.example.api.dto.LogEventPayload
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import kotlin.random.Random

val mapper = jacksonObjectMapper()
private val logger = LoggerFactory.getLogger("LogService")

class LogService(
	private val kafkaProducer: KafkaProducer<String, String>,
	private val topicName: String
) {
	private var logProducerJob: Job? = null

	fun sendLogToKafka(payload: LogEventPayload) {
		runCatching {
			val json = mapper.writeValueAsString(payload)
			kafkaProducer.send(ProducerRecord(topicName, json))
		}.onFailure { exception ->
			logger.error("Failed to send log to Kafka", exception)
		}
	}

	fun startLogProducer() {
		if (logProducerJob?.isActive == true) return

		logProducerJob = CoroutineScope(Dispatchers.IO).launch {
			val levels = listOf("INFO", "WARN", "ERROR")
			val services = listOf("auth-service", "db-service", "post-service")

			while (isActive) {
				runCatching {
					val payload = LogEvent(
						level = levels.random(),
						service = services.random(),
						message = "Random log message ${Random.nextInt(1000)}",
						timestamp = System.currentTimeMillis()
					)

					sendLogToKafka(LogEventPayload(events = listOf(payload)))
					delay(Random.nextLong(500, 3000))
				}.onFailure { exception ->
					when (exception) {
						is CancellationException -> {
							logger.warn("Log producer cancelled")
						}

						else -> {
							logger.error("Error in log producer loop", exception)
							delay(1000) // Brief pause before retrying
						}
					}
				}
			}
		}
	}

	fun stopLogProducer() {
		logProducerJob?.cancel()
		logProducerJob = null
	}

	fun close() {
		stopLogProducer()
		kafkaProducer.close()
	}
}
