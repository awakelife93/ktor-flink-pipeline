package com.example.flink.dto

data class LogEvent(
  val level: String = "UNKNOWN",
  val service: String = "UNKNOWN",
  val message: String = "",
  val timestamp: Long = 0L,
)

data class LogCount(
  val level: String = "UNKNOWN",
  val count: Int = 0,
)

data class LogEventPayload(val events: List<LogEvent>)
