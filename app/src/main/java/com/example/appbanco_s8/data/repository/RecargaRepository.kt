package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.Recarga
import com.example.appbanco_s8.data.model.RecargaRequest
import com.example.appbanco_s8.data.remote.RetrofitClient
import retrofit2.Response

class RecargaRepository {
    private val api = RetrofitClient.api

    suspend fun getRecargas(
        token: String,
        userId: String,
        limit: Int = 20,
        offset: Int = 0,
        operadora: String? = null
    ): List<Recarga> {
        val bearerToken = "Bearer $token"
        // Si operadora no es null, enviamos el filtro eq.Operadora
        val operadoraFilter = operadora?.let { "eq.$it" }
        
        val response = api.getRecargas(
            token = bearerToken,
            userId = "eq.$userId",
            limit = limit,
            offset = offset,
            operadora = operadoraFilter
        )
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    suspend fun createRecarga(token: String, request: RecargaRequest): Boolean {
        val bearerToken = "Bearer $token"
        val response = api.createRecarga(bearerToken, request)
        return response.isSuccessful
    }

    suspend fun deleteRecarga(token: String, recargaId: String): Boolean {
        val bearerToken = "Bearer $token"
        val response = api.deleteRecarga(bearerToken, "eq.$recargaId")
        return response.isSuccessful
    }
}
