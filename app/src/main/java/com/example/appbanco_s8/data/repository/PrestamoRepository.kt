
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.Prestamo
import com.example.appbanco_s8.data.remote.RetrofitClient

class PrestamoRepository {
    private val api = RetrofitClient.api

    suspend fun getPrestamos(token: String): Result<List<Prestamo>> = try {
        val r = api.getPrestamos("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}