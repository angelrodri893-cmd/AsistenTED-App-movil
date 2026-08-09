package com.asistented.app.datos.gobec

internal object SelectorTramitesGobEc {
    private const val LIMITE_TRAMITES = 8

    val idsPrioritarios = listOf(
        "11745", // Certificados y actas registrales.
        "12371", // Firma electronica: se descarta si Gob.Ec no lo marca 100% en linea.
        "11247", // Antecedentes penales.
        "11345", // Certificado del CNE.
        "1002", // Certificado de RUC.
        "15051", // Autorizacion a terceros.
        "12781", // Certificado de afiliacion IESS.
        "12967", // Historia laboral IESS.
        "3718" // Apostilla y legalizacion.
    )

    val institucionesFallback = listOf("8", "23", "58", "588", "413", "163", "6")

    fun seleccionar(candidatos: List<TramiteGobEcDto>, limite: Int = LIMITE_TRAMITES): List<TramiteGobEcDto> {
        val validos = candidatos
            .distinctBy { it.tramiteId }
            .filter(::esGeneralEnLinea)

        val porId = validos.associateBy { it.tramiteId }
        val seleccionados = idsPrioritarios
            .mapNotNull(porId::get)
            .toMutableList()

        if (seleccionados.size < limite) {
            validos
                .filterNot { candidato -> seleccionados.any { it.tramiteId == candidato.tramiteId } }
                .sortedByDescending(::puntajeFrecuencia)
                .take(limite - seleccionados.size)
                .forEach(seleccionados::add)
        }

        return seleccionados.take(limite)
    }

    fun esGeneralEnLinea(tramite: TramiteGobEcDto): Boolean {
        if (tramite.tramiteEnLineaUrl.isBlank()) return false
        if (!tramite.tramiteEnLineaCompleto.equals("Sí", ignoreCase = true)) return false
        val texto = "${tramite.nombre} ${tramite.descripcion} ${tramite.institucionUrl}".lowercase()
        return terminosLocales.none { it in texto }
    }

    private fun puntajeFrecuencia(tramite: TramiteGobEcDto): Int {
        val nombre = tramite.nombre.lowercase()
        return palabrasClaveFrecuentes.sumOf { (palabra, peso) -> if (palabra in nombre) peso else 0 }
    }

    private val palabrasClaveFrecuentes = listOf(
        "certificado" to 10,
        "ruc" to 9,
        "afiliación" to 9,
        "historia laboral" to 9,
        "antecedentes" to 8,
        "clave" to 7,
        "apostilla" to 7,
        "autorización" to 5
    )

    private val terminosLocales = listOf(
        "gad ",
        "municip",
        "cantón",
        "canton",
        "provincia",
        "provincial",
        "parroquia",
        "parroquial",
        "distrital"
    )
}
