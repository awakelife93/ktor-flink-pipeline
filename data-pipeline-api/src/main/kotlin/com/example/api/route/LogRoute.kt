package com.example.api.route

import com.example.api.dto.LogEventPayload
import com.example.api.service.LogService
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting(logService: LogService) {
	routing {
		post("/logs") {
			val payload = call.receive<LogEventPayload>()
			logService.sendLogToKafka(payload)
			call.respondText("OK")
		}

		post("/start") {
			logService.startLogProducer()
			call.respondText("Log producer started")
		}

		post("/stop") {
			logService.stopLogProducer()
			call.respondText("Log producer stopped")
		}
	}
}
