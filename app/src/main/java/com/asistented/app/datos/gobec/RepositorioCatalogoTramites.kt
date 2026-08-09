package com.asistented.app.datos.gobec

import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.datos.PreferenciasLocales
import com.asistented.app.datos.modelos.PasoGuia
import com.asistented.app.datos.modelos.Tramite
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
        val cacheAnterior = cargarTramitesCache()
        val candidatos = cargarCandidatosApi()
        val seleccionados = SelectorTramitesGobEc.seleccionar(candidatos)
        if (seleccionados.size < CANTIDAD_TRAMITES_OFICIALES) {
            return resolverCatalogoTrasRefresco(
                locales = CatalogoTramites.tramites,
                cache = cacheAnterior,
                remotos = emptyList()
            )
        }

        val enriquecidos = enriquecerConInstituciones(seleccionados)
        guardarCache(enriquecidos)
        return resolverCatalogoTrasRefresco(
            locales = CatalogoTramites.tramites,
            cache = cacheAnterior,
            remotos = enriquecidos.map { it.aTramite() }
        )
    }

    private suspend fun enriquecerConInstituciones(
        tramites: List<TramiteGobEcDto>
    ): List<TramiteGobEcDto> {
        val instituciones = tramites
            .map(TramiteGobEcDto::institucionId)
            .filter(String::isNotBlank)
            .distinct()
            .map { institucionId -> institucionId to cliente.obtenerInstitucion(institucionId) }
            .toMap()

        return tramites.map { tramite ->
            val institucion = instituciones[tramite.institucionId]
            tramite.copy(
                institucionNombre = institucion?.nombre.orEmpty(),
                institucionSiglas = institucion?.siglas.orEmpty()
            )
        }
    }

    private suspend fun cargarCandidatosApi(): List<TramiteGobEcDto> {
        val prioritarios = SelectorTramitesGobEc.idsPrioritarios
            .mapNotNull { id -> cliente.obtenerTramite(id) }

        if (SelectorTramitesGobEc.seleccionar(prioritarios).size >= 8) {
            return prioritarios
        }

        val fallback = buildList {
            SelectorTramitesGobEc.institucionesFallback.forEach { institucionId ->
                listOf(0, 1).forEach { pagina ->
                    addAll(cliente.obtenerTramitesInstitucion(institucionId, pagina))
                }
            }
        }

        return prioritarios + fallback
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
        private const val CANTIDAD_TRAMITES_OFICIALES = 8
        private const val DURACION_CACHE_MILLIS = 24 * 60 * 60 * 1000L

        fun combinarCatalogos(locales: List<Tramite>, remotos: List<Tramite>): List<Tramite> =
            remotos.distinctBy { it.id }.takeIf { it.isNotEmpty() } ?: locales.distinctBy { it.id }

        fun resolverCatalogoTrasRefresco(
            locales: List<Tramite>,
            cache: List<Tramite>,
            remotos: List<Tramite>
        ): List<Tramite> = when {
            remotos.size >= CANTIDAD_TRAMITES_OFICIALES -> remotos.distinctBy { it.id }
            cache.isNotEmpty() -> cache.distinctBy { it.id }
            else -> locales.distinctBy { it.id }
        }
    }
}

internal fun TramiteGobEcDto.aTramite(): Tramite {
    val requisitos = listOf(requisitosObligatorios, requisitosEspeciales)
        .map(TextoHtmlGobEc::limpiar)
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

    val resumen = TextoHtmlGobEc.resumen(descripcion)
        .ifBlank { "Información oficial publicada en Gob.Ec para iniciar este trámite en línea." }
    val procedimientoLimpio = TextoHtmlGobEc.limpiar(procedimiento)
    val costoLimpio = TextoHtmlGobEc.limpiar(costoDetalle).ifBlank { TextoHtmlGobEc.limpiar(costo) }
    val tituloOficial = TextoHtmlGobEc.limpiar(nombre).replace("\n", " ")
    val institucionOficial = TextoHtmlGobEc.limpiar(institucionNombre).replace("\n", " ")

    return Tramite(
        id = "gobec_$tramiteId",
        title = tituloOficial,
        institution = institucionOficial.ifBlank { nombreInstitucion(institucionId) },
        summary = resumen,
        category = categoriaInstitucion(institucionId, nombre),
        urlOficial = tramiteEnLineaUrl.ifBlank { url },
        steps = pasosOficialesGobEc(this),
        apiId = tramiteId,
        imagenUrl = imagenUrl.ifBlank { null },
        requisitosOficiales = requisitos.ifBlank { null },
        procedimientoOficial = procedimientoLimpio.ifBlank { null },
        costoOficial = costoLimpio.ifBlank { null },
        urlTramiteEnLinea = tramiteEnLineaUrl.ifBlank { null },
        actualizadoEn = TextoHtmlGobEc.extraerFecha(modificado),
        fuenteOficial = "Gob.Ec - Creative Commons Attribution"
    )
}

private fun pasosOficialesGobEc(tramite: TramiteGobEcDto): List<PasoGuia> =
    TextoHtmlGobEc.extraerPasosProcedimiento(tramite.procedimiento)
        .mapIndexed { indice, paso ->
            PasoGuia(
                id = "api_paso_${indice + 1}",
                title = tituloBrevePaso(paso.descripcion),
                description = paso.descripcion,
                textoAyuda = paso.seccion
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "Apartado oficial: $it." }
                    ?: "Realiza esta instrucción con calma en el portal oficial.",
                elementosRevision = listOf("Completé este paso."),
                espacioImagen = ""
            )
        }

private fun tituloBrevePaso(descripcion: String): String {
    val primeraIdea = descripcion
        .substringBefore('.')
        .substringBefore(';')
        .trim()
    if (primeraIdea.length <= 64) return primeraIdea
    val corte = primeraIdea.lastIndexOf(' ', startIndex = 64).takeIf { it > 24 } ?: 64
    return primeraIdea.take(corte).trimEnd() + "..."
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
