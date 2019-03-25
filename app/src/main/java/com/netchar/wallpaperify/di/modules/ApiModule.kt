package com.netchar.wallpaperify.di.modules

import com.netchar.wallpaperify.data.remote.api.PhotosApi
import com.netchar.wallpaperify.data.repositories.IPhotosRepository
import com.netchar.wallpaperify.data.repositories.PhotosRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
object ApiModule {

    @JvmStatic
    @Provides
    @Singleton
    fun providesPhotoApi(retrofit: Retrofit): PhotosApi = retrofit.create(PhotosApi::class.java)

    @JvmStatic
    @Provides
    @Singleton
    fun photosRepo(api: PhotosApi): IPhotosRepository = PhotosRepository(api)
}