package net.nprod.connector.commons


sealed class KnownError : RuntimeException()
sealed class KnownCriticalError : RuntimeException()

object APIError : KnownError()
object NonExistentReference : KnownError()
data class BadRequestError(val content: String) : KnownError()
object DecodingError : KnownError()
object TooManyRequests : KnownError()
object TimeoutException : KnownError()


data class UnManagedReturnCode(val status: Int) : KnownCriticalError()