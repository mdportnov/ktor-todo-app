package mephi.ru.model

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val id: Int,
    val userId: Int, 
    val todo: String, 
    val done: Boolean
)