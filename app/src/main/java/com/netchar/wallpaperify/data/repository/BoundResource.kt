package com.netchar.wallpaperify.data.repository

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.netchar.wallpaperify.data.models.Cause
import com.netchar.wallpaperify.data.models.Resource
import com.netchar.wallpaperify.data.remote.HttpResult
import com.netchar.wallpaperify.infrastructure.CoroutineDispatchers
import com.netchar.wallpaperify.infrastructure.extensions.awaitSafe
import kotlinx.coroutines.*
import retrofit2.Response
import java.io.IOException

abstract class BoundResource<TResult : Any>(val dispatchers: CoroutineDispatchers) : IBoundResource<TResult> {
    private val result = MutableLiveData<Resource<TResult>>()

    @VisibleForTesting
    lateinit var job: Job

    final override fun getLiveData(): LiveData<Resource<TResult>> = result

    fun launchIn(scope: CoroutineScope): IBoundResource<TResult> {
        job = scope.launch(dispatchers.main + SupervisorJob()) {
            result.value = tryGetResponse()
        }
        return this
    }

    private suspend fun tryGetResponse(): Resource<TResult> {
        return try {
            val databaseData = fetchFromDatabaseAsync()
            if (databaseData.isInvalidated()) {
                fetchFromNetworkAsync().also { writeInStorageOnSuccessAsync(it) }
            } else {
                Resource.Success(databaseData!!)
            }
        } catch (ex: IOException) {
            Resource.Error.parse(ex)
        }
    }

    private suspend fun writeInStorageOnSuccessAsync(resource: Resource<TResult>) {
        if (resource is Resource.Success) {
            withContext(dispatchers.database) {
                saveRemoteDataInStorage(resource.data)
            }
        }
    }

    final override fun cancelJob() {
        job.cancelChildren()
    }

    abstract fun saveRemoteDataInStorage(data: TResult?)

    private suspend fun fetchFromDatabaseAsync(): TResult? = withContext(dispatchers.database) {
        getStorageData()
    }

    abstract fun getStorageData(): TResult?

    abstract fun isNeedRefresh(localData: TResult): Boolean

    private suspend fun fetchFromNetworkAsync(): Resource<TResult> {
        result.value = Resource.Loading(true)
        val apiResponse = apiRequestAsync().awaitSafe()
        result.value = Resource.Loading(false)
        return when (apiResponse) {
            is HttpResult.Success -> Resource.Success(apiResponse.data)
            is HttpResult.Empty -> Resource.Error(Cause.UNEXPECTED, "Error during fetching data from server.")
            is HttpResult.Error -> Resource.Error.parse(apiResponse)
            is HttpResult.Exception -> Resource.Error(Cause.UNEXPECTED, message = apiResponse.exception.localizedMessage)
        }
    }

    abstract fun apiRequestAsync(): Deferred<Response<TResult>>

    private fun TResult?.isInvalidated() = this == null || isNeedRefresh(this)
}