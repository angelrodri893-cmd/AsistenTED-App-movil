package com.asistented.app.datos.gobec

import android.util.Log
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
            TramiteGobEcDto.fromJson(primerObjetoGobEc(leerTexto("$baseUrl/tramites/$tramiteId")))
        }.onFailure { error ->
            Log.w(TAG, "No se pudo obtener el tramite $tramiteId", error)
        }.getOrNull()
    }

    suspend fun obtenerTramitesInstitucion(institucionId: String, pagina: Int): List<TramiteGobEcDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = JSONArray(leerTexto("$baseUrl/tramites?institution=$institucionId&page=$pagina"))
                List(json.length()) { indice -> TramiteGobEcDto.fromJson(json.getJSONObject(indice)) }
            }.onFailure { error ->
                Log.w(TAG, "No se pudo obtener la pagina $pagina de la institucion $institucionId", error)
            }.getOrDefault(emptyList())
        }

    suspend fun obtenerInstitucion(institucionId: String): InstitucionGobEcDto? = withContext(Dispatchers.IO) {
        runCatching {
            InstitucionGobEcDto.fromJson(primerObjetoGobEc(leerTexto("$baseUrl/instituciones/$institucionId")))
        }.onFailure { error ->
            Log.w(TAG, "No se pudo obtener la institucion $institucionId", error)
        }.getOrNull()
    }

    private fun leerTexto(url: String): String {
        var ultimoError: Exception? = null
        repeat(2) { intento ->
            try {
                return leerTextoUnaVez(url)
            } catch (error: Exception) {
                ultimoError = error
                if (intento == 0) Thread.sleep(350)
            }
        }
        throw ultimoError ?: IllegalStateException("No se pudo consultar Gob.Ec")
    }

    private fun leerTextoUnaVez(url: String): String {
        val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Accept-Language", "es-EC,es;q=0.9")
            setRequestProperty("User-Agent", "AsistenTED/1.0 (Android; consulta ciudadana Gob.Ec)")
            setRequestProperty("Connection", "close")
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

    private companion object {
        const val TAG = "ClienteGobEc"
    }
}

internal fun primerObjetoGobEc(textoJson: String): JSONObject {
    val texto = textoJson.trim()
    return if (texto.startsWith("[")) {
        JSONArray(texto).optJSONObject(0) ?: error("Gob.Ec devolvio una lista vacia")
    } else {
        JSONObject(texto)
    }
}
