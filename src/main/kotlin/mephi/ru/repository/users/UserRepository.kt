package mephi.ru.repository.users

import mephi.ru.model.User

interface UserRepository {
    suspend fun addUser(
        email: String, name: String, passwordHash: String
    ): User?

    suspend fun findUser(userId: Int): User?
    suspend fun findUserByEmail(email: String): User?
}