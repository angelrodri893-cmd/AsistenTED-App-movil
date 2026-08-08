package com.asistented.app.datos.gobec

import org.json.JSONObject

internal data class TramiteGobEcDto(
    val tramiteId: String,
    val nombre: String,
    val url: String,
    val institucionId: String,
    val institucionUrl: String,
    val imagenUrl: String,
    val descripcion: String,
    val requisitosObligatorios: String,
    val requisitosEspeciales: String,
    val procedimiento: String,
    val tramiteEnLineaUrl: String,
    val tramiteEnLineaCompleto: String,
    val costo: String,
    val modificado: String
) {
    fun toJson(): JSONObject = JSONObject()
        .put("tramite_id", tramiteId)
        .put("nombre", nombre)
        .put("url", url)
        .put("institucion_id", institucionId)
        .put("institucion_url", institucionUrl)
        .put("imagen_url", imagenUrl)
        .put("descripcion", descripcion)
        .put("requisitos_obligatorios", requisitosObligatorios)
        .put("requisitos_especiales", requisitosEspeciales)
        .put("procedimiento", procedimiento)
        .put("tramite_enlinea_url", tramiteEnLineaUrl)
        .put("tramite_enlinea_completo", tramiteEnLineaCompleto)
        .put("costo", costo)
        .put("modificado", modificado)

    companion object {
        fun fromJson(json: JSONObject): TramiteGobEcDto = TramiteGobEcDto(
            tramiteId = json.optString("tramite_id"),
            nombre = json.optString("nombre"),
            url = normalizarUrlGobEc(json.optString("url")),
            institucionId = json.optString("institucion_id"),
            institucionUrl = normalizarUrlGobEc(json.optString("institucion_url")),
            imagenUrl = normalizarUrlGobEc(json.optString("imagen_url")),
            descripcion = json.optString("descripcion"),
            requisitosObligatorios = json.optString("requisitos_obligatorios"),
            requisitosEspeciales = json.optString("requisitos_especiales"),
            procedimiento = json.optString("procedimiento"),
            tramiteEnLineaUrl = normalizarUrlGobEc(json.optString("tramite_enlinea_url")),
            tramiteEnLineaCompleto = json.optString("tramite_enlinea_completo"),
            costo = json.optString("costo"),
            modificado = json.optString("modificado")
        )
    }
}

internal data class InstitucionGobEcDto(
    val institucionId: String,
    val nombre: String,
    val siglas: String,
    val logo: String,
    val website: String
) {
    companion object {
        fun fromJson(json: JSONObject): InstitucionGobEcDto = InstitucionGobEcDto(
            institucionId = json.optString("institucion_id"),
            nombre = json.optString("institucion"),
            siglas = json.optString("siglas"),
            logo = normalizarUrlGobEc(json.optString("logo")),
            website = normalizarUrlGobEc(json.optString("website"))
        )
    }
}

private fun normalizarUrlGobEc(valor: String): String {
    val limpia = valor.trim()
    return when {
        limpia.startsWith("https://www.gob.ec//") -> limpia.replace("https://www.gob.ec//", "https://www.gob.ec/")
        limpia.startsWith("//") -> "https:$limpia"
        else -> limpia
    }
}
