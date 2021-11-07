package mephi.ru.repository.users

import mephi.ru.database.Users
import mephi.ru.database.dbQuery
import mephi.ru.model.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement

class UserRepositoryImpl : UserRepository {
    private fun rowToUser(row: ResultRow?): User? {
        if (row == null) {
            return null
        }

        return User(
            userId = row[Users.userId],
            email = row[Users.email],
            displayName = row[Users.displayName],
            passwordHash = row[Users.passwordHash]
        )
    }

    override suspend fun addUser(
        email: String,
        name: String,
        passwordHash: String
    ): User? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            val isExists = Users.select {
                exists(Users.select { Users.email eq email })
            }.singleOrNull()

            if (isExists != null)
                throw UserAlreadyExistsException()

            statement = Users.insert { user ->
                user[Users.email] = email
                user[Users.displayName] = name
                user[Users.passwordHash] = passwordHash
            }
        }
        return rowToUser(statement?.resultedValues?.get(0))
    }

    override suspend fun findUser(userId: Int) = dbQuery {
        Users.select { Users.userId.eq(userId) }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun findUserByEmail(email: String) = dbQuery {
        Users.select { Users.email.eq(email) }
            .map { rowToUser(it) }.singleOrNull()
    }
}