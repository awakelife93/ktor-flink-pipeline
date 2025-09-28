package com.example.api.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class ErrorResponse(
  val error: String,
  val message: String,
  val path: String? = null,
  @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val timestamp: LocalDateTime = LocalDateTime.now(),
  val status: Int
) {
  companion object {
    fun of(
      error: String,
      message: String? = null,
      path: String? = null,
      status: Int = 500
    ): ErrorResponse {
      return ErrorResponse(
        error = error,
        message = message ?: "An unknown server error occurred.",
        path = path,
        status = status
      )
    }

    fun internalServerError(
      message: String? = null,
      path: String? = null
    ): ErrorResponse {
      return of(
        error = "Internal Server Error",
        message = message,
        path = path,
        status = 500
      )
    }

    fun badRequest(
      message: String? = null,
      path: String? = null
    ): ErrorResponse {
      return of(
        error = "Bad Request",
        message = message,
        path = path,
        status = 400
      )
    }
  }
}
