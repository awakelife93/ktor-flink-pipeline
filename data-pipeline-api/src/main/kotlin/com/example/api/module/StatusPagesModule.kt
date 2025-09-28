package com.example.api.module

import com.example.api.dto.ErrorResponse
import com.fasterxml.jackson.databind.JsonMappingException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPagesModule")

fun Application.configureStatusPages() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      val requestPath = call.request.local.uri

      when (cause) {
        is JsonMappingException -> {
          logger.warn("Bad request at $requestPath: ${cause.message}")
          call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.badRequest(cause.message, requestPath)
          )
        }

        else -> {
          logger.error("Unhandled exception at $requestPath", cause)
          call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse.internalServerError(cause.message, requestPath)
          )
        }
      }
    }
  }
}
