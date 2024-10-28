package com.example.weather.result

sealed class ResponseResult<T : Any> {
    data class Success<T : Any>(val data: T) : ResponseResult<T>()
    data class Error<T : Any>(val code: Int, val message: String?) : ResponseResult<T>()
    data class Exception<T : Any>(val exception: Throwable) : ResponseResult<T>()
}