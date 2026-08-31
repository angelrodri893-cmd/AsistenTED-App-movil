package com.asistented.app.interfaz

import com.asistented.app.datos.modelos.PasoGuia
import com.asistented.app.datos.modelos.Tramite

// Datos exclusivos de las vistas previas de Android Studio; nunca se usan como catálogo de la aplicación.
internal val tramitesDeVistaPrevia = listOf(
    tramiteDeVistaPrevia("1002", "Registro Único de Contribuyentes (RUC)", "SRI", "Tributario"),
    tramiteDeVistaPrevia("11745", "Emisión de certificados y actas registrales", "Registro Civil", "Identidad"),
    tramiteDeVistaPrevia("12781", "Certificado de afiliación", "IESS", "Seguridad social")
)

private fun tramiteDeVistaPrevia(
    id: String,
    titulo: String,
    institucion: String,
    categoria: String
) = Tramite(
    id = "gobec_$id",
    title = titulo,
    institution = institucion,
    summary = "Información de ejemplo para visualizar la tarjeta del trámite.",
    category = categoria,
    urlOficial = "https://www.gob.ec/tramites/$id",
    urlTramiteEnLinea = "https://www.gob.ec/tramites/$id/webform",
    steps = listOf(
        PasoGuia(
            id = "paso_1",
            title = "Ingresar al portal oficial",
            description = "Abre el sitio oficial de la institución.",
            textoAyuda = "Verifica que la dirección corresponda a la institución.",
            elementosRevision = listOf("Abrí el portal oficial."),
            espacioImagen = ""
        ),
        PasoGuia(
            id = "paso_2",
            title = "Completar la solicitud",
            description = "Revisa los requisitos y completa la información solicitada.",
            textoAyuda = "Lee cada campo antes de enviarlo.",
            elementosRevision = listOf("Revisé la información."),
            espacioImagen = ""
        )
    ),
    apiId = id
)
