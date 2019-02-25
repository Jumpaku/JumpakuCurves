package jumpaku.curves.core.util

sealed class Result<V: Any> {

    fun value(): Option<V> = if (this is Success) some(value) else none()

    fun error(): Option<Exception> = if (this is Failure) some(error) else none()

    val isSuccess: Boolean get() = this is Success

    val isFailure: Boolean get() = this is Failure

    fun <U: Any> tryMap(transform: (V) -> U): Result<U> = tryFlatMap { result { transform(it) } }

    fun <U: Any> tryFlatMap(transform: (V) -> Result<U>): Result<U> = when (this) {
        is Success -> try { transform(value) } catch (e: Exception) { Failure<U>(e) }
        is Failure -> Failure(error)
    }

    fun tryRecover(recovery: (Exception) -> V): Result<V> = when (this) {
        is Success -> this
        is Failure -> result { recovery(error) }
    }

    fun tryMapFailure(transform: (Exception) -> Exception): Result<V> = when (this) {
        is Success -> this
        is Failure -> Failure(try { transform(error) } catch (e: Exception) { e })
    }

    fun orThrow(): V = when (this) {
        is Success -> value
        is Failure -> throw error
    }
}

class Success<V: Any>(val value: V) : Result<V>() {

    override fun toString(): String = "Success($value)"
}

class Failure<V: Any>(val error: Exception) : Result<V>() {

    override fun toString(): String = "Failure($error)"
}

fun <T: Any> Result<Result<T>>.flatten(): Result<T> = tryFlatMap { it }

fun <T: Any> result(tryCompute: () -> T): Result<T> = try { Success(tryCompute()) } catch (e: Exception) { Failure(e) }

fun <V: Any> success(value: V): Result<V> = Success(value)

fun <V: Any> failure(exception: Exception): Result<V> = Failure(exception)
