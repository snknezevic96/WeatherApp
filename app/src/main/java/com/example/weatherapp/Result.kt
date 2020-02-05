package com.example.weatherapp

sealed class Result<out T : Any> {

    data class Loading<out T : Any>(val data: T?) : Result<T>()
    data class Success<out T : Any>(val data: T) : Result<T>()

    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Loading<*> -> "Loading[data=$data]"
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}