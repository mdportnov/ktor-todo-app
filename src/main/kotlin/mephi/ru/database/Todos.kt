package mephi.ru.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Todos : Table() {
    val id = integer("id").autoIncrement()
    val userId: Column<Int> = integer("userId").references(Users.userId)
    val todo = varchar("todo", 512)
    val done = bool("done")
    override val primaryKey = PrimaryKey(id, name = "PK_Todos_ID")
}