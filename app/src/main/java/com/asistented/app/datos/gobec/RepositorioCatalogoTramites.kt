package com.asistented.app.datos.gobec

import com.asistented.app.datos.modelos.PasoGuia
import com.asistented.app.datos.modelos.Tramite

internal class RepositorioCatalogoTramites(
    private val cliente: ClienteGobEc = ClienteGobEc()
) {
    suspend fun cargarCatalogo(): List<Tramite> {
        val candidatos = cargarCandidatosApi()
        val seleccionados = SelectorTramitesGobEc.seleccionar(candidatos)
        if (seleccionados.size < CANTIDAD_TRAMITES_OFICIALES) {
            throw CatalogoNoDisponibleException()
        }

        val enriquecidos = enriquecerConInstituciones(seleccionados)
        return enriquecidos
            .map { it.aTramite() }
            .distinctBy { it.id }
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

        if (SelectorTramitesGobEc.seleccionar(prioritarios).size >= CANTIDAD_TRAMITES_OFICIALES) {
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

    internal companion object {
        private const val CANTIDAD_TRAMITES_OFICIALES = 8
    }
}

internal class CatalogoNoDisponibleException : IllegalStateException(
    "No se pudo obtener un catálogo oficial completo desde Gob.Ec."
)

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
