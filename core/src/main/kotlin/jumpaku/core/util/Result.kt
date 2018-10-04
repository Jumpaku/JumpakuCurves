package jumpaku.core.util

sealed class Result<V> {

    fun value(): Option<V> = if (this is Success) some(value) else none()

    fun error(): Option<Exception> = if (this is Failure) some(error) else none()

    val isSuccess: Boolean get() = this is Success

    val isFailure: Boolean get() = this is Failure

    fun <U> tryMap(transform: (V) -> U): Result<U> = tryFlatMap { result { transform(it) } }

    fun <U> tryFlatMap(transform: (V) -> Result<U>): Result<U> = when (this) {
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
}

class Success<V>(val value: V) : Result<V>() {

    override fun toString(): String = "Success($value)"
}

class Failure<V>(val error: Exception) : Result<V>() {

    override fun toString(): String = "Failure($error)"
}

fun <T> Result<Result<T>>.flatten(): Result<T> = tryFlatMap { it }

fun <T> result(tryCompute: () -> T): Result<T> = try { Success(tryCompute()) } catch (e: Exception) { Failure(e) }

fun <V> success(value: V): Result<V> = Success(value)

fun <V> failure(exception: Exception): Result<V> = Failure(exception)
