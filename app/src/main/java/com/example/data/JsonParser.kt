package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonParser {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    inline fun <reified T> toJson(value: T): String {
        return try {
            val adapter = moshi.adapter(T::class.java)
            adapter.toJson(value)
        } catch (e: Exception) {
            ""
        }
    }

    inline fun <reified T> fromJson(json: String): T? {
        return try {
            if (json.isEmpty()) return null
            val adapter = moshi.adapter(T::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun <T> toJsonList(list: List<T>, type: Class<T>): String {
        val listType = Types.newParameterizedType(List::class.java, type)
        val adapter = moshi.adapter<List<T>>(listType)
        return adapter.toJson(list)
    }

    fun <T> fromJsonList(json: String, type: Class<T>): List<T> {
        return try {
            if (json.isEmpty()) return emptyList()
            val listType = Types.newParameterizedType(List::class.java, type)
            val adapter = moshi.adapter<List<T>>(listType)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
