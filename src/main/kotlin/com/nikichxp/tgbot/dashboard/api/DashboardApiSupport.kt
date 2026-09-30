package com.nikichxp.tgbot.dashboard.api

import com.nikichxp.tgbot.dashboard.dto.DashboardPrincipal
import com.nikichxp.tgbot.dashboard.dto.ErrorResponse
import com.nikichxp.tgbot.dashboard.error.DashboardConflictException
import com.nikichxp.tgbot.dashboard.error.DashboardForbiddenException
import com.nikichxp.tgbot.dashboard.error.DashboardNotFoundException
import com.nikichxp.tgbot.dashboard.error.DashboardUnauthorizedException
import com.nikichxp.tgbot.dashboard.service.DashboardAuthService
import com.nikichxp.tgbot.people.error.PeopleListAlreadyExistsException
import com.nikichxp.tgbot.people.error.PeopleListNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.server.ResponseStatusException

@Component
class DashboardApiSupport(private val authService: DashboardAuthService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun authenticate(request: ServerRequest) {
        val token = request.bearerToken() ?: throw DashboardUnauthorizedException()
        request.attributes()[PRINCIPAL_ATTRIBUTE] = authService.authenticate(token)
    }

    fun principal(request: ServerRequest): DashboardPrincipal =
        request.attributes()[PRINCIPAL_ATTRIBUTE] as DashboardPrincipal

    suspend fun errorResponse(error: Throwable): ServerResponse {
        val (status, message) = when (error) {
            is DashboardUnauthorizedException -> 401 to error.message
            is DashboardForbiddenException -> 403 to error.message
            is DashboardNotFoundException, is PeopleListNotFoundException -> 404 to error.message
            is DashboardConflictException, is PeopleListAlreadyExistsException -> 409 to error.message
            is IllegalArgumentException -> 400 to error.message
            is ResponseStatusException -> error.statusCode.value() to error.reason
            else -> {
                logger.error("Dashboard request failed", error)
                500 to "Internal error"
            }
        }
        return ServerResponse.status(status).bodyValueAndAwait(ErrorResponse(message ?: "Error"))
    }

    companion object {
        private const val PRINCIPAL_ATTRIBUTE = "dashboardPrincipal"
    }
}

fun ServerRequest.bearerToken(): String? =
    headers().firstHeader(HttpHeaders.AUTHORIZATION)
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.substring("Bearer ".length)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
