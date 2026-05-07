
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.*
import com.example.appbanco_s8.data.remote.RetrofitClient

class CuentaRepository {
    private val api = RetrofitClient.api

    suspend fun getCuentas(token: String): Result<List<Cuenta>> = try {
        val r = api.getCuentas("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getCuentasByUser(token: String, userId: String): Result<List<Cuenta>> = try {
        val r = api.getCuentasByUser("Bearer $token", "eq.$userId")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getTransacciones(token: String, cuentaId: String): Result<List<Transaccion>> = try {
        val r = api.getTransacciones("Bearer $token", "eq.$cuentaId")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getCuentaAhorro(token: String): Result<CuentaAhorro?> = try {
        val r = api.getCuentaAhorro("Bearer $token")
        if (r.isSuccessful) Result.success(r.body()?.firstOrNull())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getTransaccionesByCuenta(token: String, cuentaId: String): Result<List<Transaccion>> {
        return try {
            val bearerToken = "Bearer $token"
            val response = api.getTransacciones(bearerToken, "eq.$cuentaId")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener transacciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun realizarTransferencia(
        token: String,
        idOrigen: String,
        nuevoSaldoOrigen: Double,
        idDestino: String,
        nuevoSaldoDestino: Double,
        monto: Double
    ): Result<Unit> {
        return try {
            val bearerToken = "Bearer $token"
            val now = java.time.OffsetDateTime.now().toString()

            // 1. Actualizar saldo origen (PATCH)
            api.updateSaldo(bearerToken, "eq.$idOrigen", UpdateSaldoRequest(nuevoSaldoOrigen))
            
            // 2. Actualizar saldo destino (PATCH)
            api.updateSaldo(bearerToken, "eq.$idDestino", UpdateSaldoRequest(nuevoSaldoDestino))

            // 3. Registrar transacción de salida (Origen)
            api.createTransaccion(bearerToken, TransaccionRequest(
                cuentaId = idOrigen,
                tipo = "TRANSFERENCIA ENVIADA",
                monto = monto,
                descripcion = "A cuenta propia",
                fecha = now
            ))

            // 4. Registrar transacción de entrada (Destino)
            api.createTransaccion(bearerToken, TransaccionRequest(
                cuentaId = idDestino,
                tipo = "TRANSFERENCIA RECIBIDA",
                monto = monto,
                descripcion = "Desde cuenta propia",
                fecha = now
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}