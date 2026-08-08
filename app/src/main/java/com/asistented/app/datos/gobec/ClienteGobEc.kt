package com.asistented.app.datos.gobec

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal class ClienteGobEc(
    private val baseUrl: String = "https://www.gob.ec/api/v1"
) {
    suspend fun obtenerTramite(tramiteId: String): TramiteGobEcDto? = withContext(Dispatchers.IO) {
        runCatching {
            TramiteGobEcDto.fromJson(JSONObject(leerTexto("$baseUrl/tramites/$tramiteId")))
        }.getOrNull()
    }

    suspend fun obtenerTramitesInstitucion(institucionId: String, pagina: Int): List<TramiteGobEcDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = JSONArray(leerTexto("$baseUrl/tramites?institution=$institucionId&page=$pagina"))
                List(json.length()) { indice -> TramiteGobEcDto.fromJson(json.getJSONObject(indice)) }
            }.getOrDefault(emptyList())
        }

    suspend fun obtenerInstitucion(institucionId: String): InstitucionGobEcDto? = withContext(Dispatchers.IO) {
        runCatching {
            InstitucionGobEcDto.fromJson(JSONObject(leerTexto("$baseUrl/instituciones/$institucionId")))
        }.getOrNull()
    }

    private fun leerTexto(url: String): String {
        val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val codigo = conexion.responseCode
            val stream = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
            val texto = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (codigo !in 200..299) error("Gob.Ec respondio HTTP $codigo")
            texto
        } finally {
            conexion.disconnect()
        }
    }
}
