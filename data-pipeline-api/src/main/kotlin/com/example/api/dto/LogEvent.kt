package com.example.api.dto

data class LogEvent(
  val level: String = "UNKNOWN",
  val service: String = "UNKNOWN",
  val message: String = "",
  val timestamp: Long = 0L,
)

data class LogEventPayload(val events: List<LogEvent>)
