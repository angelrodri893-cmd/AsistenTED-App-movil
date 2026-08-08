package com.asistented.app.datos.gobec

internal object TextoHtmlGobEc {
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

    private fun String.decodificarEntidadesHtml(): String {
        var salida = this
        entidades.forEach { (entidad, reemplazo) -> salida = salida.replace(entidad, reemplazo) }
        return salida.replace(Regex("&#(\\d+);")) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
        }
    }

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
