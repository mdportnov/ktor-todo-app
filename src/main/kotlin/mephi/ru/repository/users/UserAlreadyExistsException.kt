package mephi.ru.repository.users

class UserAlreadyExistsException : Throwable() {
    override val message: String
        get() = "User Already Exists"
}
