package com.netchar.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.netchar.common.utils.Connectivity
import com.netchar.common.utils.CoroutineDispatchers
import com.netchar.remote.Resource
import com.netchar.remote.enums.Cause
import com.netchar.remote.extensions.awaitSafe
import kotlinx.coroutines.*
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

abstract class BoundResource<TResult : Any>(private val dispatchers: CoroutineDispatchers, val context: Context) : IBoundResource<TResult> {
    private val result = MutableLiveData<Resource<TResult>>()

    @VisibleForTesting
    lateinit var job: Job

    final override fun getLiveData(): LiveData<Resource<TResult>> = result

    fun launchIn(scope: CoroutineScope): IBoundResource<TResult> {
        job = scope.launch(dispatchers.main) {
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
            Timber.e(ex)
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
        if (job.isActive) {
            job.cancel()
        }
    }

    abstract fun saveRemoteDataInStorage(data: TResult)

    private suspend fun fetchFromDatabaseAsync(): TResult? = withContext(dispatchers.database) {
        getStorageData()
    }

    abstract fun getStorageData(): TResult?

    abstract fun isNeedRefresh(localData: TResult): Boolean

    private suspend fun fetchFromNetworkAsync(): Resource<TResult> {
        return if (Connectivity.isInternetAvailable(context)) {
            result.value = Resource.Loading(true)
            val apiResponse = apiRequestAsync().awaitSafe()
            result.value = Resource.Loading(false)

            when (apiResponse) {
                is com.netchar.remote.enums.HttpResult.Success -> Resource.Success(apiResponse.data)
                is com.netchar.remote.enums.HttpResult.Empty -> Resource.Error(Cause.UNEXPECTED, "Error during fetching data from server.")
                is com.netchar.remote.enums.HttpResult.Error -> Resource.Error.parse(apiResponse)
                is com.netchar.remote.enums.HttpResult.Exception -> Resource.Error(Cause.UNEXPECTED, message = apiResponse.exception.localizedMessage)
            }
        } else {
            Resource.Error(Cause.NO_INTERNET_CONNECTION, context.getString(R.string.error_message_no_internet))
        }
    }

    abstract fun apiRequestAsync(): Deferred<Response<TResult>>

    private fun TResult?.isInvalidated() = this == null || isNeedRefresh(this)
}