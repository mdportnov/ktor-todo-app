package mephi.ru.repository.todos

import mephi.ru.model.Todo

interface TodoRepository {
    suspend fun addTodo(userId: Int, todo: String, done: Boolean): Todo?
    suspend fun getTodos(userId: Int): List<Todo>
}