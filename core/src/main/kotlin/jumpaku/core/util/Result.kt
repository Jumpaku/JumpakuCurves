package jumpaku.core.util

sealed class Result<V> {

    fun value(): Option<V> = if (this is Success) some(value) else none()

    fun error(): Option<Exception> = if (this is Failure) some(error) else none()

    fun <U> map(transform: (V) -> U): Result<U> = flatMap { result { transform(it) } }

    fun mapFailure(transform: (Exception) -> Exception): Result<V> = flatMapFailure {
        Failure(try { transform(it) } catch (e: Exception) { e })
    }

    fun <U> flatMap(transform: (V) -> Result<U>): Result<U> = when (this) {
        is Success -> try { transform(value) } catch (e: Exception) { Failure<U>(e) }
        is Failure -> Failure(error)
    }

    fun flatMapFailure(transform: (Exception) -> Result<V>): Result<V> = when (this) {
        is Failure -> try { transform(error) } catch (e: Exception) { Failure<V>(e) }
        is Success -> this
    }

    fun recover(recover: (Throwable) -> V): Result<V> = when (this) {
        is Success -> this
        is Failure -> result { recover(error) }
    }

    fun orThrow(): V = when(this) {
        is Success -> value
        is Failure -> throw error
    }
}

class Success<V>(val value: V) : Result<V>() {

    override fun toString(): String = "Success($value)"
}

class Failure<V>(val error: Exception) : Result<V>() {

    override fun toString(): String = "Failure($error)"
}

fun <T> result(tryCompute: () -> T): Result<T> = try { Success(tryCompute()) } catch (e: Exception) { Failure(e) }

fun <V> success(value: V): Result<V> = Success(value)

fun <V> failure(exception: Exception): Result<V> = Failure(exception)
