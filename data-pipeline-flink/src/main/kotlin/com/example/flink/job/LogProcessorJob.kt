package com.example.flink.job

import com.example.flink.dto.LogCount
import com.example.flink.module.configureFlinkModule
import com.example.flink.module.logStreamExecutionEnvironment
import com.example.flink.service.calculateLogCount
import com.example.flink.service.generateLogEvent
import com.example.flink.service.keyByLogLevel
import org.apache.flink.streaming.api.datastream.KeyedStream
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class LogProcessorJob(
	private val kafkaTopic: String,
	private val kafkaBootstrapServers: String
) {
	private val logger: Logger = LoggerFactory.getLogger(this::class.java)

	fun run() {
		logger.info("Starting Flink Log Processor Job...")

		runCatching {
			val dataStream =
				configureFlinkModule(kafkaTopic, kafkaBootstrapServers, "flink-log-consumer", "Kafka Log Event Source")

			val logEventStream = generateLogEvent(dataStream)
			val keyedStream = keyByLogLevel(logEventStream)
			val logCountStream = calculateLogCount(keyedStream)

			logger.info("Successfully configured Flink data stream processing.")

			setupSinks(logCountStream, keyedStream)

			logStreamExecutionEnvironment.execute("Log Level Count Processor")
		}.onFailure { exception ->
			logger.error("Failed to start Flink Log Processor Job", exception)
		}
	}

	private fun setupSinks(logCountStream: SingleOutputStreamOperator<LogCount>, keyedStream: KeyedStream<LogCount, String>) {
		// Print total log counts sink
		logCountStream.print("LogCounts")

		// Setup error alert sink
		setupErrorAlertSink(keyedStream)
	}

	private fun setupErrorAlertSink(keyedStream: KeyedStream<LogCount, String>) {
		val logCount5MinutesStream = calculateLogCount(keyedStream, Duration.ofMinutes(5))

		// Alert when ERROR logs exceed threshold (50+) sink
		logCount5MinutesStream
			.filter { it.level == "ERROR" && it.count >= 50 }
			.print("ErrorAlerts")
	}
}
