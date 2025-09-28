package com.example.flink.service

import com.example.flink.dto.LogCount
import com.example.flink.dto.LogEvent
import com.example.flink.dto.LogEventPayload
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.datastream.KeyedStream
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.util.Collector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

val mapper = jacksonObjectMapper()
private val logger: Logger = LoggerFactory.getLogger("LogProcessingService")

fun generateLogEvent(dataStream: DataStream<String>): DataStream<LogEvent> {
  return dataStream.flatMap { payload: String, out: Collector<LogEvent> ->
    runCatching {
      val logPayload: LogEventPayload = mapper.readValue(payload)
      logPayload.events.forEach { event -> out.collect(event) }
    }.onFailure { exception ->
      logger.error("Failed to parse log payload: $payload", exception)
    }
  }.returns(LogEvent::class.java)
}

fun keyByLogLevel(logEventStream: DataStream<LogEvent>): KeyedStream<LogCount, String> {
  return logEventStream
    .map { event -> LogCount(level = event.level, count = 1) }
    .keyBy { it.level }
}

fun calculateLogCount(
  keyedStream: KeyedStream<LogCount, String>,
  time: Duration? = null
): SingleOutputStreamOperator<LogCount> =
  time?.let {
    keyedStream
      .window(TumblingProcessingTimeWindows.of(it))
      .reduce { a, b -> LogCount(a.level, a.count + b.count) }
  } ?: keyedStream
    .reduce { a, b -> LogCount(a.level, a.count + b.count) }