package mephi.ru.model

data class User(
    val userId: Int,
    val email: String,
    val displayName: String,
    val passwordHash: String
)