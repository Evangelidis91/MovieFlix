package com.evangelidis.movieflix.data.remote.interceptor

import com.evangelidis.movieflix.data.NetworkConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val urlWithApiKey = originalUrl.newBuilder()
            .addQueryParameter("api_key", NetworkConstants.API_KEY)
            .build()

        val authenticatedRequest = originalRequest.newBuilder()
            .url(urlWithApiKey)
            .addHeader("Accept", "application/json")
            .build()

        return chain.proceed(authenticatedRequest)
    }


}