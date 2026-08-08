package com.asistented.app.datos.gobec

import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.datos.PreferenciasLocales
import com.asistented.app.datos.modelos.PasoGuia
import com.asistented.app.datos.modelos.Tramite
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray

internal class RepositorioCatalogoTramites(
    private val preferenciasLocales: PreferenciasLocales,
    private val cliente: ClienteGobEc = ClienteGobEc(),
    private val relojMillis: () -> Long = { System.currentTimeMillis() }
) {
    fun cargarCatalogoConCache(): List<Tramite> =
        combinarCatalogos(CatalogoTramites.tramites, cargarTramitesCache())

    fun requiereRefresco(): Boolean {
        val cache = preferenciasLocales.cargarCacheTramitesGobEc()
        val guardadoEn = preferenciasLocales.cargarFechaCacheTramitesGobEc()
        return cache.isNullOrBlank() || relojMillis() - guardadoEn > DURACION_CACHE_MILLIS
    }

    suspend fun refrescarCatalogo(): List<Tramite> {
        val candidatos = cargarCandidatosApi()
        val seleccionados = SelectorTramitesGobEc.seleccionar(candidatos)
        if (seleccionados.isNotEmpty()) {
            guardarCache(seleccionados)
        }
        return combinarCatalogos(CatalogoTramites.tramites, seleccionados.map { it.aTramite() })
    }

    private suspend fun cargarCandidatosApi(): List<TramiteGobEcDto> = coroutineScope {
        val prioritarios = SelectorTramitesGobEc.idsPrioritarios
            .map { id -> async { cliente.obtenerTramite(id) } }
            .awaitAll()
            .filterNotNull()

        if (SelectorTramitesGobEc.seleccionar(prioritarios).size >= 8) {
            return@coroutineScope prioritarios
        }

        val fallback = SelectorTramitesGobEc.institucionesFallback.flatMap { institucionId ->
            listOf(0, 1).map { pagina ->
                async { cliente.obtenerTramitesInstitucion(institucionId, pagina) }
            }
        }.awaitAll().flatten()

        prioritarios + fallback
    }

    private fun cargarTramitesCache(): List<Tramite> {
        val json = preferenciasLocales.cargarCacheTramitesGobEc().orEmpty()
        if (json.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            List(array.length()) { indice ->
                TramiteGobEcDto.fromJson(array.getJSONObject(indice)).aTramite()
            }
        }.getOrDefault(emptyList())
    }

    private fun guardarCache(tramites: List<TramiteGobEcDto>) {
        val array = JSONArray()
        tramites.forEach { array.put(it.toJson()) }
        // El catalogo local queda siempre disponible; el cache solo acelera y sostiene los datos oficiales sin internet.
        preferenciasLocales.guardarCacheTramitesGobEc(array.toString(), relojMillis())
    }

    internal companion object {
        private const val DURACION_CACHE_MILLIS = 24 * 60 * 60 * 1000L

        fun combinarCatalogos(locales: List<Tramite>, remotos: List<Tramite>): List<Tramite> =
            remotos.distinctBy { it.id }.takeIf { it.isNotEmpty() } ?: locales.distinctBy { it.id }
    }
}

internal fun TramiteGobEcDto.aTramite(): Tramite {
    val requisitos = listOf(requisitosObligatorios, requisitosEspeciales)
        .map(TextoHtmlGobEc::limpiar)
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

    val resumen = TextoHtmlGobEc.resumen(descripcion)
        .ifBlank { "Información oficial publicada en Gob.Ec para iniciar este trámite en línea." }

    return Tramite(
        id = "gobec_$tramiteId",
        title = nombre.trim(),
        institution = nombreInstitucion(institucionId),
        summary = resumen,
        category = categoriaInstitucion(institucionId, nombre),
        urlOficial = tramiteEnLineaUrl.ifBlank { url },
        steps = pasosDidacticosGobEc(this, requisitos),
        apiId = tramiteId,
        imagenUrl = imagenUrl.ifBlank { null },
        requisitosOficiales = requisitos.ifBlank { null },
        urlTramiteEnLinea = tramiteEnLineaUrl.ifBlank { null },
        actualizadoEn = modificado.ifBlank { null },
        fuenteOficial = "Gob.Ec - Creative Commons Attribution"
    )
}

private fun pasosDidacticosGobEc(tramite: TramiteGobEcDto, requisitos: String): List<PasoGuia> {
    val nombre = tramite.nombre.lowercase()
    val documentos = requisitos.lineSequence().firstOrNull { it.isNotBlank() } ?: "documentos o datos indicados por Gob.Ec"
    return listOf(
        PasoGuia(
            id = "revisar",
            title = "1. Revisar la información oficial",
            description = "Lee el resumen del trámite y confirma que corresponde a lo que necesitas hacer.",
            textoAyuda = "Si el nombre del trámite no coincide con tu necesidad, vuelve al inicio y revisa otra opción.",
            elementosRevision = listOf(
                "Leí el nombre completo del trámite.",
                "Confirmé la institución responsable.",
                "Revisé si el trámite se realiza en línea."
            ),
            espacioImagen = "Espacio para captura del trámite oficial: $nombre."
        ),
        PasoGuia(
            id = "requisitos",
            title = "2. Preparar requisitos",
            description = "Ten a la mano los requisitos base antes de abrir el portal oficial.",
            textoAyuda = "Requisito principal a revisar: $documentos",
            elementosRevision = listOf(
                "Revisé los requisitos oficiales.",
                "Tengo mis documentos o datos personales a mano.",
                "Anoté cualquier clave o usuario que pueda necesitar."
            ),
            espacioImagen = "Espacio para imagen de documentos o requisitos."
        ),
        PasoGuia(
            id = "portal",
            title = "3. Abrir el trámite en línea",
            description = "Presiona el botón del portal oficial y verifica que la dirección pertenezca a la institución.",
            textoAyuda = "Evita abrir enlaces enviados por desconocidos. Usa el botón oficial de la app.",
            elementosRevision = listOf(
                "Abrí el enlace desde la app.",
                "Verifiqué que el sitio sea oficial.",
                "No compartí mi contraseña con otra persona."
            ),
            espacioImagen = "Espacio para captura de la página oficial."
        ),
        PasoGuia(
            id = "formulario",
            title = "4. Completar datos",
            description = "Llena los campos con calma. Revisa nombres, números, fechas y correos antes de avanzar.",
            textoAyuda = "Un dato mal escrito puede hacer que el trámite se rechace o se demore.",
            elementosRevision = listOf(
                "Escribí mis datos sin errores.",
                "Revisé los campos obligatorios.",
                "Guardé cualquier código que apareció en pantalla."
            ),
            espacioImagen = "Espacio para captura del formulario."
        ),
        PasoGuia(
            id = "guardar",
            title = "5. Guardar comprobante",
            description = "Al finalizar, descarga o toma captura del comprobante, número de solicitud o resultado.",
            textoAyuda = "No cierres la página hasta guardar una constancia del trámite.",
            elementosRevision = listOf(
                "Revisé el resumen final.",
                "Guardé el comprobante o número de solicitud.",
                "Anoté si debo revisar una respuesta después."
            ),
            espacioImagen = "Espacio para imagen del comprobante final."
        )
    )
}

private fun nombreInstitucion(id: String): String = when (id) {
    "6" -> "Cancillería"
    "8" -> "SRI"
    "23" -> "Registro Civil"
    "163" -> "IESS"
    "413" -> "CNE"
    "588" -> "Ministerio del Interior"
    "58" -> "Ministerio de Gobierno"
    else -> "Gob.Ec"
}

private fun categoriaInstitucion(id: String, nombre: String): String = when {
    id == "8" -> "Tributario"
    id == "23" -> "Identidad"
    id == "163" -> "Seguridad social"
    id == "413" -> "Ciudadanía"
    id == "588" || id == "58" -> "Seguridad"
    id == "6" -> "Documentos"
    "certificado" in nombre.lowercase() -> "Certificados"
    else -> "Trámite en línea"
}
