package com.netchar.remote.api

import com.netchar.models.Photo
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PhotosApi {

    @GET("photos")
    fun getPhotosAsync(
            @Query("page") page: Int,
            @Query("per_page") per_page: Int,
            @Query("order_by") order_by: String = ""
    ): Deferred<Response<List<Photo>>>

    @GET("photos/{id}")
    fun getPhotoAsync(@Path("id") id: String) : Deferred<Response<Photo>>
}