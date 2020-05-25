package models

data class User(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null
) {
    fun isValid(): Boolean {
        return username != null && password != null && email != null
    }
}
