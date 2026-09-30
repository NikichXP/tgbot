package com.nikichxp.tgbot.dashboard.api

import com.nikichxp.tgbot.dashboard.dto.CreatePeopleListRequest
import com.nikichxp.tgbot.dashboard.dto.UpdatePeopleListRequest
import com.nikichxp.tgbot.dashboard.service.DashboardPeopleService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.time.Duration

@Configuration
class DashboardPeopleController(
    private val peopleService: DashboardPeopleService,
    private val apiSupport: DashboardApiSupport
) {

    @Bean
    fun dashboardPeopleRouter() = coRouter {
        "/admin".nest {
            filter { request, next ->
                apiSupport.authenticate(request)
                next(request)
            }

            GET("/people") { request ->
                val text = request.queryParam("query").orElse(null)
                val limit = request.queryParam("limit").map { it.toIntOrNull() }.orElse(null) ?: DEFAULT_SEARCH_LIMIT
                ok().bodyValueAndAwait(peopleService.searchPeople(text, limit))
            }

            GET("/people/{id}/avatar") { request ->
                val avatar = peopleService.avatar(request.userIdPathVariable("id"))
                    ?: return@GET noContent().buildAndAwait()
                ok().contentType(MediaType.IMAGE_JPEG)
                    .cacheControl(CacheControl.maxAge(AVATAR_BROWSER_CACHE).cachePrivate())
                    .bodyValueAndAwait(avatar)
            }

            GET("/people-lists") {
                ok().bodyValueAndAwait(peopleService.lists())
            }

            POST("/people-lists") { request ->
                val body = request.awaitBody<CreatePeopleListRequest>()
                ok().bodyValueAndAwait(peopleService.createList(body.name, body.description))
            }

            GET("/people-lists/{name}") { request ->
                ok().bodyValueAndAwait(peopleService.list(request.pathVariable("name")))
            }

            PUT("/people-lists/{name}") { request ->
                val body = request.awaitBody<UpdatePeopleListRequest>()
                ok().bodyValueAndAwait(peopleService.updateListDescription(request.pathVariable("name"), body.description))
            }

            DELETE("/people-lists/{name}") { request ->
                peopleService.deleteList(request.pathVariable("name"))
                noContent().buildAndAwait()
            }

            PUT("/people-lists/{name}/members/{userId}") { request ->
                ok().bodyValueAndAwait(
                    peopleService.addListMember(request.pathVariable("name"), request.userIdPathVariable("userId"))
                )
            }

            DELETE("/people-lists/{name}/members/{userId}") { request ->
                ok().bodyValueAndAwait(
                    peopleService.removeListMember(request.pathVariable("name"), request.userIdPathVariable("userId"))
                )
            }
        }

        onError<Exception> { error, _ -> apiSupport.errorResponse(error) }
    }

    private fun ServerRequest.userIdPathVariable(name: String): Long =
        pathVariable(name).toLongOrNull() ?: throw IllegalArgumentException("User id must be a number")

    companion object {
        private const val DEFAULT_SEARCH_LIMIT = 50
        private val AVATAR_BROWSER_CACHE = Duration.ofHours(1)
    }
}
