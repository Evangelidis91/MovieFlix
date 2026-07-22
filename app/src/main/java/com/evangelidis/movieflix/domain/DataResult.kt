package com.evangelidis.movieflix.domain

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val throwable: Throwable) : DataResult<Nothing>
    object Loading : DataResult<Nothing>
}