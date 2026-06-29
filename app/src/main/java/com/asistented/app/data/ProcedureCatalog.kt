package com.asistented.app.data

import com.asistented.app.data.model.GuideStep
import com.asistented.app.data.model.Procedure

object ProcedureCatalog {
    val procedures: List<Procedure> = listOf(
        Procedure(
            id = "cedula",
            title = "Renovación u obtención de cédula",
            institution = "Registro Civil",
            category = "Identidad",
            officialUrl = "https://www.registrocivil.gob.ec/",
            summary = "Guía para revisar requisitos, agendar turno y preparar documentos para la cédula.",
            steps = baseSteps(
                procedure = "cédula",
                portal = "portal del Registro Civil",
                documents = "cédula anterior, comprobante de pago y datos personales actualizados"
            )
        ),
        Procedure(
            id = "ruc",
            title = "Registro Único de Contribuyentes (RUC)",
            institution = "SRI",
            category = "Tributario",
            officialUrl = "https://www.sri.gob.ec/",
            summary = "Guía para ubicar el servicio de RUC, revisar requisitos y completar el proceso en línea.",
            steps = baseSteps(
                procedure = "RUC",
                portal = "portal del SRI",
                documents = "cédula, certificado de votación y datos de actividad económica"
            )
        ),
        Procedure(
            id = "impuestos",
            title = "Declaración de impuestos en línea",
            institution = "SRI",
            category = "Tributario",
            officialUrl = "https://srienlinea.sri.gob.ec/",
            summary = "Guía para entrar a SRI en línea, elegir el formulario correcto y revisar antes de enviar.",
            steps = baseSteps(
                procedure = "declaración de impuestos",
                portal = "SRI en línea",
                documents = "clave del SRI, comprobantes, facturas y periodo a declarar"
            )
        ),
        Procedure(
            id = "iess",
            title = "Afiliación y consulta al IESS",
            institution = "IESS",
            category = "Seguridad social",
            officialUrl = "https://www.iess.gob.ec/",
            summary = "Guía para consultar afiliación, servicios en línea y datos personales en el IESS.",
            steps = baseSteps(
                procedure = "consulta o afiliación del IESS",
                portal = "portal del IESS",
                documents = "cédula, clave del IESS y datos de contacto"
            )
        ),
        Procedure(
            id = "licencia",
            title = "Obtención o renovación de licencia",
            institution = "ANT",
            category = "Movilidad",
            officialUrl = "https://www.ant.gob.ec/",
            summary = "Guía para revisar requisitos, generar turno y preparar pagos para licencia de conducir.",
            steps = baseSteps(
                procedure = "licencia de conducir",
                portal = "portal de la ANT",
                documents = "cédula, licencia anterior si aplica, comprobante de pago y turno"
            )
        ),
        Procedure(
            id = "pasaporte",
            title = "Solicitud de pasaporte",
            institution = "Registro Civil",
            category = "Identidad",
            officialUrl = "https://www.registrocivil.gob.ec/",
            summary = "Guía para revisar requisitos, pagar y agendar la emisión del pasaporte.",
            steps = baseSteps(
                procedure = "pasaporte",
                portal = "portal del Registro Civil",
                documents = "cédula vigente, comprobante de pago y turno agendado"
            )
        )
    )

    fun findProcedure(id: String): Procedure? = procedures.firstOrNull { it.id == id }

    private fun baseSteps(
        procedure: String,
        portal: String,
        documents: String
    ): List<GuideStep> = listOf(
        GuideStep(
            id = "preparar",
            title = "1. Preparar lo necesario",
            description = "Antes de entrar al sitio oficial, reúne los datos y documentos que normalmente se solicitan para el trámite de $procedure.",
            helpText = "Ten todo a la mano. Si no entiendes un documento, pide ayuda antes de continuar.",
            checkItems = listOf(
                "Revisé que tengo conexión a internet.",
                "Tengo a la mano: $documents.",
                "Anoté mi usuario o clave del portal si ya la tengo."
            ),
            imagePlaceholder = "Espacio para imagen: documentos necesarios para $procedure."
        ),
        GuideStep(
            id = "entrar",
            title = "2. Entrar al portal oficial",
            description = "Abre el enlace oficial y confirma que estás en el $portal. Evita páginas compartidas por mensajes o redes sociales.",
            helpText = "Si la dirección no parece oficial o pide datos extraños, no continúes.",
            checkItems = listOf(
                "Abrí el portal desde el botón oficial.",
                "Verifiqué que el sitio pertenece a la institución.",
                "No compartí mi clave con otra persona."
            ),
            imagePlaceholder = "Espacio para captura: pantalla inicial del portal oficial."
        ),
        GuideStep(
            id = "buscar",
            title = "3. Buscar el servicio correcto",
            description = "Dentro del portal, busca el servicio relacionado con $procedure. Lee despacio los nombres de los botones antes de presionar.",
            helpText = "Si hay varias opciones parecidas, revisa la descripción o vuelve al inicio del portal.",
            checkItems = listOf(
                "Encontré el servicio relacionado con el trámite.",
                "Leí las instrucciones principales.",
                "Confirmé si necesito agendar, pagar o llenar un formulario."
            ),
            imagePlaceholder = "Espacio para imagen: menú o botón del servicio."
        ),
        GuideStep(
            id = "llenar",
            title = "4. Completar la información",
            description = "Llena los campos solicitados con calma. Revisa nombres, números de cédula, fechas y correos antes de avanzar.",
            helpText = "Los datos deben coincidir con tus documentos. Un número mal escrito puede detener el trámite.",
            checkItems = listOf(
                "Escribí mis datos sin errores.",
                "Revisé los campos obligatorios.",
                "Guardé o anoté cualquier código de solicitud."
            ),
            imagePlaceholder = "Espacio para captura: formulario del trámite."
        ),
        GuideStep(
            id = "finalizar",
            title = "5. Revisar y finalizar",
            description = "Antes de enviar, revisa el resumen del trámite. Si todo está correcto, confirma y guarda el comprobante.",
            helpText = "Nunca cierres la página sin guardar el comprobante, turno o número de solicitud.",
            checkItems = listOf(
                "Revisé el resumen antes de enviar.",
                "Guardé el comprobante, turno o número de trámite.",
                "Anoté la fecha de seguimiento si corresponde."
            ),
            imagePlaceholder = "Espacio para imagen: comprobante o pantalla final."
        )
    )
}
