package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class Recarga(
    val id: String = "",
    @SerializedName("user_id")
    val userId: String = "",
    val operadora: String = "",
    val celular: String = "",
    val monto: Double = 0.0,
    val fecha: String = ""
) {
    fun celularEnmascarado(): String {
        return if (celular.length >= 4) {
            "**** " + celular.takeLast(4)
        } else {
            celular
        }
    }

    fun montoFormateado() = "S/ %,.2f".format(monto)

    fun obtenerMesAnio(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(fecha)
            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            outputFormat.format(date ?: Date()).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Desconocido"
        }
    }
}

data class RecargaRequest(
    @SerializedName("user_id")
    val userId: String,
    val operadora: String,
    val celular: String,
    val monto: Double
)
