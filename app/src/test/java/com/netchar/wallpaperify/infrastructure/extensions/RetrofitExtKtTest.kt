package com.netchar.wallpaperify.infrastructure.extensions

import com.netchar.wallpaperify.data.remote.HttpResult
import com.netchar.wallpaperify.data.remote.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.io.IOException

/**
 * Created by Netchar on 04.04.2019.
 * e.glushankov@gmail.com
 */
internal class RetrofitExtKtTest {

    private val mockResponse = mockk<Deferred<Response<String>>>()

    @Test
    fun `Ensure await() calls`() {
        coEvery { mockResponse.await() } returns Response.success("Some body")
        runBlocking { mockResponse.awaitSafe() }
        coVerify { mockResponse.await() }
    }

    @Test
    fun `When response success should return success HttpResult`() {
        coEvery { mockResponse.await() } returns Response.success("Some body")
        runBlocking {
            val response = mockResponse.awaitSafe()
            assertTrue { response is HttpResult.Success && response.data == "Some body" }
        }
    }

    @Test
    fun `Response with empty body and NoContent status code should return empty HttpResult`() {
        coEvery { mockResponse.await() } returns Response.success(HttpStatusCode.NO_CONTENT.code, "")
        runBlocking {
            val response = mockResponse.awaitSafe()
            assertTrue { response is HttpResult.Empty }
        }
    }

    @Test
    fun `Response with empty body should return exception HttpResult`() {
        coEvery { mockResponse.await() } returns Response.success("")
        runBlocking {
            val response = mockResponse.awaitSafe()
            assertTrue { response is HttpResult.Exception }
        }
    }

    @Test
    fun `Response with null body should return exception HttpResult`() {
        coEvery { mockResponse.await() } returns Response.success(null)
        runBlocking {
            val response = mockResponse.awaitSafe()
            assertTrue { response is HttpResult.Exception }
        }
    }

    @Test
    fun `Invalid Response should return error HttpResult`() {
        coEvery { mockResponse.await() } returns mockk(relaxed = true) {
            every { isSuccessful } returns false
            every { code() } returns HttpStatusCode.INTERNAL_SERVER_ERROR.code
            every { errorBody() } returns null
        }
        runBlocking {
            val response = mockResponse.awaitSafe()
            assertTrue { response is HttpResult.Error && response.message == HttpStatusCode.INTERNAL_SERVER_ERROR.description }
        }
    }

    @Test
    fun `When response throws exception returns exception HttpResult`() {
        coEvery { mockResponse.await() } throws IOException("Some IO exception")
        runBlocking {
            val response = mockResponse.awaitSafe()
            assertTrue { response is HttpResult.Exception && response.exception.message == "Some IO exception" }
        }
    }
}