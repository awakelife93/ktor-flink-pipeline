package com.example.api.module

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.request.header
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.uri
import org.slf4j.event.Level
import java.util.UUID

fun Application.configureRequestLogging() {

  // Install CallId plugin for request tracking
  install(CallId) {
    header("X-Request-ID")
    generate { UUID.randomUUID().toString().substring(0, 8) }
  }

  // Install DoubleReceive plugin to enable reading request body multiple times
  install(DoubleReceive)

  // Install CallLogging plugin
  install(CallLogging) {
    level = Level.INFO

    filter { call ->
      !call.request.path().startsWith("/actuator") &&
          !call.request.path().startsWith("/health")
    }

    format { call ->
      val requestId = call.callId ?: "unknown"
      val method = call.request.httpMethod.value
      val uri = call.request.uri
      val status = call.response.status()?.value ?: 0
      val processingTime = call.processingTimeMillis()
      val clientIp = call.request.header("X-Forwarded-For")
        ?: call.request.header("X-Real-IP")
        ?: call.request.local.remoteHost
      val userAgent = call.request.header("User-Agent")?.take(50)

      if (processingTime > 1000) {
        call.application.environment.log.warn("SLOW_REQUEST: [$requestId] $method $uri took ${processingTime}ms")
      }

      if (status >= 400) {
        call.application.environment.log.warn("ERROR_RESPONSE: [$requestId] $method $uri returned $status")
      }

      "[$requestId] $method $uri - $status - ${processingTime}ms - $clientIp - $userAgent"
    }
  }
}
