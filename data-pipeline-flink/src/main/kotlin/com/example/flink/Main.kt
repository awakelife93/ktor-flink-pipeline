package com.example.flink

import com.example.flink.dto.Job
import com.example.flink.factory.JobFactory
import com.example.flink.factory.KafkaConfigSelector
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val KAFKA_TOPIC = "logs"
private val KAFKA_BOOTSTRAP_SERVERS = KafkaConfigSelector.kafkaBootstrapServers

private val logger: Logger = LoggerFactory.getLogger("FlinkLogProcessor")

fun main() {
	logger.info("Starting Flink Log Processor Application...")

	val jobs = initializeJobs()
	runJobs(jobs)

	logger.info("Flink Log Processor Application startup completed.")
}

private fun initializeJobs(): Job {
	logger.info("Initializing Jobs...")

	return Job(
		listOf(
			"LogProcessor" to JobFactory.createLogProcessorJob(
				kafkaTopic = KAFKA_TOPIC,
				kafkaBootstrapServers = KAFKA_BOOTSTRAP_SERVERS
			)
		)
	)
}

private fun runJobs(jobs: Job) {
	logger.info("Executing ${jobs.size} job(s)...")

	jobs.forEach { (jobName, jobRunner) ->
		logger.info("Starting job: $jobName")

		runJob(jobName, jobRunner)
	}
}

private fun runJob(name: String, action: () -> Unit) {
	runCatching { action() }
		.onSuccess { logger.info("Job completed successfully: $name") }
		.onFailure { logger.error("Job execution failed: $name", it) }
}
