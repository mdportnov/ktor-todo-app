package mephi.ru.plugins

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import mephi.ru.auth.JwtService
import mephi.ru.auth.MySession
import mephi.ru.repository.users.UserRepository
import mephi.ru.repository.users.UserAlreadyExistsException

const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val USER_LOGIN = "$USERS/login"
const val USER_CREATE = "$USERS/create"

@Location(USER_LOGIN)
class UserLoginRoute

@Location(USER_CREATE)
class UserCreateRoute

fun Route.users(db: UserRepository, jwtService: JwtService, hashF: (String) -> String) {
    val missingFields  = "Missing Fields"

    post<UserCreateRoute> {
        val signupParameters = call.receive<Parameters>()
        val password = signupParameters["password"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, missingFields)
        val displayName = signupParameters["displayName"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, missingFields)
        val email = signupParameters["email"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, missingFields)

        val hash = hashF(password)
        try {
            val newUser = db.addUser(email, displayName, hash)
            newUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respondText(jwtService.generateToken(newUser),
                    status = HttpStatusCode.Created)
            }
        } catch (e: UserAlreadyExistsException) {
            application.log.error(e.message)
            call.respond(HttpStatusCode.Forbidden, e.message)
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems creating User")
        }
    }

    post<UserLoginRoute> {
        val signingParameters = call.receive<Parameters>()
        val password = signingParameters["password"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, missingFields)
        val email = signingParameters["email"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, missingFields)

        val hash = hashF(password)
        try {
            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                if (currentUser.passwordHash == hash) {
                    call.sessions.set(MySession(it))
                    call.respondText(jwtService.generateToken(currentUser))
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest, "Problems retrieving User"
                    )
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }
}