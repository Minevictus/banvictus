package com.proximyst.banvictus.fetching

import com.github.kevinsawicki.http.HttpRequest
import com.google.common.cache.CacheBuilder
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

object AshconApi {
    private const val API_BASE = "https://api.ashcon.app/mojang/v2"
    private const val API_USER = "$API_BASE/user"
    private val GSON = Gson()

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build<String, AshconUser>()

    fun fetchUser(query: String): AshconUser? {
        val cachedValue = cache.getIfPresent(query)
        if (cachedValue != null) {
            return cachedValue
        }

        val url = "$API_USER/${query.replace("-", "")}"
        val response = HttpRequest.get(url)
        if (!response.ok()) {
            return null
        }

        return runCatching {
            GSON.fromJson(response.body(), AshconUser::class.java)
        }
            .onSuccess {
                cache.put(query, it)
                println(it.toString())
            }
            .onFailure {
                it.printStackTrace()
            }
            .getOrNull()
    }
}