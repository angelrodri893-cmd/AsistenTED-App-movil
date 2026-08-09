package com.asistented.app.datos.gobec

internal object TextoHtmlGobEc {
    data class PasoProcedimiento(
        val descripcion: String,
        val seccion: String?
    )

    fun limpiar(valor: String): String {
        if (valor.isBlank()) return ""
        // La API mezcla HTML real con entidades escapadas; por eso se decodifica antes y despues de quitar etiquetas.
        return valor
            .decodificarEntidadesHtml()
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>|</li>|</div>|</h\\d>"), "\n")
            .replace(Regex("(?i)<li[^>]*>"), "- ")
            .replace(Regex("<[^>]+>"), " ")
            .decodificarEntidadesHtml()
            .replace('\u00A0', ' ')
            .lines()
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
    }

    fun resumen(valor: String, limite: Int = 220): String {
        val texto = limpiar(valor).replace("\n", " ")
        if (texto.length <= limite) return texto
        val corteNatural = texto.indexOf('.', startIndex = 80).takeIf { it in 80 until limite }
        val corte = corteNatural ?: texto.lastIndexOf(' ', limite).takeIf { it > 0 } ?: limite
        return texto.take(corte).trimEnd('.', ',', ';') + "."
    }

    fun extraerPasosProcedimiento(valor: String): List<PasoProcedimiento> {
        val lineas = limpiar(valor).lines().map(String::trim).filter(String::isNotBlank)
        if (lineas.isEmpty()) return emptyList()

        var seccionActual: String? = null
        val pasos = buildList {
            lineas.forEach { linea ->
                val numerado = PATRON_PASO_NUMERADO.matchEntire(linea)
                when {
                    numerado != null -> add(
                        PasoProcedimiento(
                            descripcion = numerado.groupValues[2].trim(),
                            seccion = seccionActual
                        )
                    )
                    linea.startsWith("- ") -> add(
                        PasoProcedimiento(
                            descripcion = linea.removePrefix("- ").trim(),
                            seccion = seccionActual
                        )
                    )
                    linea.endsWith(":") -> seccionActual = linea.removeSuffix(":").trim()
                }
            }
        }

        if (pasos.isEmpty()) {
            return lineas
                .filterNot { it.endsWith(":") }
                .map { PasoProcedimiento(descripcion = it, seccion = seccionActual) }
        }

        // Algunos tramites mezclan instrucciones presenciales y virtuales; la app conserva solo el canal en linea.
        val haySeccionPresencial = pasos.any { it.seccion.normalizada().contains("presencial") }
        val pasosEnLinea = pasos.filter { paso ->
            val seccion = paso.seccion.normalizada()
            "virtual" in seccion || "en linea" in seccion || seccion == "linea"
        }
        return if (haySeccionPresencial && pasosEnLinea.isNotEmpty()) pasosEnLinea else pasos
    }

    fun extraerFecha(valor: String): String? {
        val fechaEnAtributo = PATRON_FECHA_DATETIME.find(valor)?.groupValues?.getOrNull(1)
        return fechaEnAtributo?.take(10) ?: limpiar(valor).takeIf { it.isNotBlank() }?.take(10)
    }

    private fun String.decodificarEntidadesHtml(): String {
        var salida = this
        entidades.forEach { (entidad, reemplazo) -> salida = salida.replace(entidad, reemplazo) }
        return salida.replace(Regex("&#(\\d+);")) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
        }
    }

    private fun String?.normalizada(): String = orEmpty()
        .lowercase()
        .replace('á', 'a')
        .replace('é', 'e')
        .replace('í', 'i')
        .replace('ó', 'o')
        .replace('ú', 'u')

    private val PATRON_PASO_NUMERADO = Regex("^(\\d+)\\s*[.)-]?\\s*(.+)$")
    private val PATRON_FECHA_DATETIME = Regex("(?i)datetime=\\\"([^\\\"]+)\\\"")

    private val entidades = mapOf(
        "&nbsp;" to " ",
        "&amp;" to "&",
        "&quot;" to "\"",
        "&#39;" to "'",
        "&lt;" to "<",
        "&gt;" to ">",
        "&aacute;" to "á",
        "&eacute;" to "é",
        "&iacute;" to "í",
        "&oacute;" to "ó",
        "&uacute;" to "ú",
        "&Aacute;" to "Á",
        "&Eacute;" to "É",
        "&Iacute;" to "Í",
        "&Oacute;" to "Ó",
        "&Uacute;" to "Ú",
        "&ntilde;" to "ñ",
        "&Ntilde;" to "Ñ",
        "&uuml;" to "ü",
        "&Uuml;" to "Ü"
    )
}
