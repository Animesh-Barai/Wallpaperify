package com.netchar.wallpaperify.data.models

import com.netchar.wallpaperify.data.models.dto.UnsplashError
import com.netchar.wallpaperify.data.remote.HttpStatusCode
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response

open class HttpResult<out T> {
    data class Success<out T>(val data: T?) : HttpResult<T>()
    data class Error(val httpStatusCode: HttpStatusCode, val error: UnsplashError?) : HttpResult<Nothing>() {
        companion object {
            private val converter by lazy {
                Moshi.Builder().build().adapter(UnsplashError::class.java)
            }

            private fun ResponseBody?.toUnsplashError(): UnsplashError? = this?.let { converter.fromJson(it.source()) }

            fun <T> parse(response: Response<T>): Error {
                val statusCode = HttpStatusCode.getByCode(response.code())
                return Error(statusCode, response.errorBody().toUnsplashError())
            }
        }
    }

    data class Exception(val exception: Throwable) : HttpResult<Nothing>()
}