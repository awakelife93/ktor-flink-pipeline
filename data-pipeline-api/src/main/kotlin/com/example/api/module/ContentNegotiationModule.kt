package com.example.api.module

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun Application.configureContentNegotiation() {
  install(ContentNegotiation) {
    jackson {
      registerKotlinModule()
      enable(SerializationFeature.INDENT_OUTPUT)
      enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
  }
}
