package com.evangelidis.movieflix.data.remote.interceptor

import com.evangelidis.movieflix.data.NetworkConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Appends the TMDB api_key parameter to every outgoing network request. */
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("api_key", NetworkConstants.API_KEY)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}