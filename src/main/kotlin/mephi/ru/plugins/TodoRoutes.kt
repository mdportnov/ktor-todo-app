package mephi.ru.plugins

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import mephi.ru.auth.MySession
import mephi.ru.repository.todos.TodoRepository
import mephi.ru.repository.users.UserRepository

const val TODOS = "$API_VERSION/todos"

@Location(TODOS)
class TodoRoute

fun Route.todos(dbUser: UserRepository, dbTodo: TodoRepository) {
    authenticate {
        post<TodoRoute> {
            val todoParams = call.receive<Parameters>()
            val todo = todoParams["todo"] ?: return@post call.respond(
                HttpStatusCode.BadRequest, "Missing Todo"
            )
            val done = todoParams["done"] ?: "false"

            val user = call.sessions.get<MySession>()?.let {
                dbUser.findUser(it.userId)
            }

            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
                return@post
            }

            try {
                val currentTodo = dbTodo.addTodo(
                    user.userId, todo, done.toBoolean()
                )
                currentTodo?.id?.let {
                    call.respond(HttpStatusCode.OK, currentTodo)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to add todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
            }
        }

        get<TodoRoute> {
            val user = call.sessions.get<MySession>()?.let { dbUser.findUser(it.userId) }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
                return@get
            }
            try {
                val todos = dbTodo.getTodos(user.userId)
                call.respond(todos)
            } catch (e: Throwable) {
                application.log.error("Failed to get Todos", e)
                call.respond(HttpStatusCode.BadRequest, "Problems getting Todos")
            }
        }

    }
}