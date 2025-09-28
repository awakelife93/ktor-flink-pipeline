package com.example.flink.job

import com.example.flink.dto.LogCount
import com.example.flink.dto.LogEvent
import com.example.flink.module.configureFlinkModule
import com.example.flink.module.logStreamExecutionEnvironment
import com.example.flink.service.calculateLogCount
import com.example.flink.service.calculateLogCountProcessingTime
import com.example.flink.service.generateLogEvent
import com.example.flink.service.keyByLogLevel
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.datastream.KeyedStream
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

			logger.info("Successfully configured Flink data stream processing.")

			setupSinks(logEventStream)

			logStreamExecutionEnvironment.execute("Log Level Count Processor")
		}.onFailure { exception ->
			logger.error("Failed to start Flink Log Processor Job", exception)
		}
	}

	private fun setupSinks(
		logEventStream: DataStream<LogEvent>
	) {
		val keyedStream = keyByLogLevel(logEventStream)

		// Setup total log count sink
		setupTotalLogCountsSink(keyedStream)

		// Setup processing time error alert sink
		setupProcessingTimeErrorAlertSink(keyedStream)
	}

	private fun setupTotalLogCountsSink(keyedStream: KeyedStream<LogCount, String>) {
		calculateLogCount(keyedStream)
			.name("TotalLogCountsSink")
			.uid("TotalLogCountsSink")
			.print("TotalLogCountsSink")
	}

	private fun setupProcessingTimeErrorAlertSink(keyedStream: KeyedStream<LogCount, String>) {
		val logCount5MinutesStreamForProcessingTime = calculateLogCountProcessingTime(keyedStream, Duration.ofMinutes(5))

		logCount5MinutesStreamForProcessingTime
			.name("ProcessingTimeErrorAlertSink")
			.uid("ProcessingTimeErrorAlertSink")
			.filter { it.level == "ERROR" && it.count >= 50 }
			.print("ProcessingTimeErrorAlertSink")
	}
}
