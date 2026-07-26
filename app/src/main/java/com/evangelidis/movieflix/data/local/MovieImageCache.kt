package com.evangelidis.movieflix.data.local

import android.content.Context
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class MovieImageCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imageLoader by lazy {
        SingletonImageLoader.get(context)
    }

    suspend fun prefetch(imageUrls: List<String>) {
        supervisorScope {
            imageUrls
                .filter(String::isNotBlank)
                .distinct()
                .map { imageUrl ->
                    async {
                        try {
                            val request = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .diskCacheKey(imageUrl)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                .build()

                            when (imageLoader.execute(request)) {
                                is SuccessResult -> Unit
                                is ErrorResult -> Unit
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Failure to cache one image should not fail page loading.
                        }
                    }
                }
                .awaitAll()
        }
    }
}