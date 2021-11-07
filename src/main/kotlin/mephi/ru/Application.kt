package mephi.ru

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import mephi.ru.auth.JwtService
import mephi.ru.auth.MySession
import mephi.ru.auth.hash
import mephi.ru.database.DatabaseFactory
import mephi.ru.model.Todo
import mephi.ru.plugins.todos
import mephi.ru.plugins.users
import mephi.ru.repository.todos.TodoRepositoryImpl
import mephi.ru.repository.users.UserRepositoryImpl

data class PrincipalUser(val userId: String) : Principal

    fun main() {
        embeddedServer(Netty) {
            install(ContentNegotiation) {
                json()
            }

            install(Locations)

            DatabaseFactory.init()
            val dbUser = UserRepositoryImpl()
            val dbTodo = TodoRepositoryImpl()
            val jwtService = JwtService()
            val hashFunction = { s: String -> hash(s) }

            install(Authentication) {
                jwt {
                    verifier(jwtService.verifier)
                    validate { credential ->
                        val userId = credential.payload
                            .getClaim("id").asInt()
                        val user = dbUser.findUser(userId)
                        PrincipalUser(user?.userId!!.toString())
                    }
                }
            }

            install(Sessions) {
                cookie<MySession>("MY_SESSION") {
                    cookie.extensions["SameSite"] = "lax"
                }
            }

            routing {
                users(dbUser, jwtService, hashFunction)
                todos(dbUser, dbTodo)
            }

        }.start()
    }
