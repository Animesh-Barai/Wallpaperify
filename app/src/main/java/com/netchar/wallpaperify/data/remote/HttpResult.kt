package com.netchar.wallpaperify.data.remote

import com.netchar.wallpaperify.data.remote.dto.UnsplashError
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response

sealed class HttpResult<out T> {
    object Empty : HttpResult<Nothing>()
    data class Success<out T>(val data: T?) : HttpResult<T>()
    data class Error(val httpStatusCode: HttpStatusCode, val error: UnsplashError?, val message: String = "") : HttpResult<Nothing>() {
        companion object {
            private val converter by lazy {
                Moshi.Builder().build().adapter(UnsplashError::class.java)
            }

            fun <T> parse(response: Response<T>): Error {
                val statusCode = HttpStatusCode.getByCode(response.code())
                return Error(statusCode, response.errorBody().toUnsplashError(), statusCode.description)
            }

            private fun ResponseBody?.toUnsplashError(): UnsplashError? = this?.let { converter.fromJson(it.source()) }
        }
    }

    data class Exception(val exception: Throwable) : HttpResult<Nothing>()
}