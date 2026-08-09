package com.asistented.app

import com.asistented.app.datos.modelos.ElementoHistorial
import com.asistented.app.datos.modelos.PerfilUsuario
import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.datos.gobec.RepositorioCatalogoTramites
import com.asistented.app.datos.gobec.SelectorTramitesGobEc
import com.asistented.app.datos.gobec.TextoHtmlGobEc
import com.asistented.app.datos.gobec.TramiteGobEcDto
import com.asistented.app.datos.gobec.aTramite
import com.asistented.app.dominio.ReglasAutenticacion
import com.asistented.app.dominio.ReglasProgreso
import com.asistented.app.dominio.ReglasContenidoUsuario
import com.asistented.app.interfaz.filtrarTramites
import com.asistented.app.interfaz.filtrarTramitesNotificaciones
import com.asistented.app.interfaz.combinarFechaHora
import com.asistented.app.interfaz.calcularProgresoDetalle
import com.asistented.app.interfaz.construirTextoGuia
import com.asistented.app.interfaz.seleccionarTramitesFavoritos
import com.asistented.app.interfaz.seleccionarTramitesHistorial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ReglasUsuarioTest {
    @Test
    fun usuarioAEmailInterno_normalizaSinExponerCorreo() {
        assertEquals("alexis_01@asistented.local", ReglasAutenticacion.usuarioAEmailInterno(" Alexis_01 "))
    }

    @Test
    fun validaciones_rechazanContrasenaCortaYAceptanUsuarioValido() {
        assertNull(ReglasAutenticacion.validarUsuario("liliana.vega"))
        assertTrue(ReglasAutenticacion.validarContrasena("123").orEmpty().contains("6"))
        assertFalse(ReglasAutenticacion.validarContrasena("123456", "123456").orEmpty().contains("no coinciden"))
    }

    @Test
    fun alternarPaso_marcaYDesmarcaPaso() {
        val marcado = ReglasProgreso.alternarPaso(emptySet(), "preparar")
        assertTrue(marcado.contains("preparar"))

        val desmarcado = ReglasProgreso.alternarPaso(marcado, "preparar")
        assertTrue(desmarcado.isEmpty())
    }

    @Test
    fun alternarFavorito_agregaYQuitaTramite() {
        val agregado = ReglasContenidoUsuario.alternarFavorito(emptyList(), "cedula")
        assertEquals(listOf("cedula"), agregado)

        val eliminado = ReglasContenidoUsuario.alternarFavorito(agregado, "cedula")
        assertTrue(eliminado.isEmpty())
    }

    @Test
    fun registrarHistorial_mueveTramiteRepetidoAlInicio() {
        val inicial = listOf(
            ElementoHistorial("ruc", 100),
            ElementoHistorial("cedula", 50)
        )
        val actualizado = ReglasContenidoUsuario.registrarHistorial(inicial, "cedula", 200)

        assertEquals("cedula", actualizado.first().tramiteId)
        assertEquals(2, actualizado.size)
        assertEquals(200, actualizado.first().consultadoEnMillis)
    }

    @Test
    fun perfilNuevo_usaAvatarLocalPorDefecto() {
        val perfil = PerfilUsuario(
            uid = "uid-1",
            username = "alexis",
            nombre = "Alexis",
            apellido = "Rodriguez"
        )

        assertEquals("azul", perfil.avatarId)
    }

    @Test
    fun filtrarTramites_buscaPorNombreEInstitucion() {
        val porNombre = filtrarTramites(CatalogoTramites.tramites, "LICENCIA", null)
        val porInstitucion = filtrarTramites(CatalogoTramites.tramites, "", "SRI")

        assertEquals(listOf("licencia"), porNombre.map { it.id })
        assertEquals(listOf("ruc", "impuestos"), porInstitucion.map { it.id })
    }

    @Test
    fun filtrarTramites_devuelveTodoOVacioSegunLaConsulta() {
        val todos = filtrarTramites(CatalogoTramites.tramites, "   ", null)
        val sinResultados = filtrarTramites(CatalogoTramites.tramites, "tramite inexistente", null)

        assertEquals(CatalogoTramites.tramites, todos)
        assertTrue(sinResultados.isEmpty())
    }

    @Test
    fun seleccionarTramitesFavoritos_conservaElOrdenDelCatalogo() {
        val seleccionados = seleccionarTramitesFavoritos(
            tramites = CatalogoTramites.tramites,
            favoritos = setOf("licencia", "cedula", "id-inexistente")
        )

        assertEquals(listOf("cedula", "licencia"), seleccionados.map { it.id })
    }

    @Test
    fun filtrarTramitesNotificaciones_buscaPorTituloOInstitucion() {
        val porTitulo = filtrarTramitesNotificaciones(CatalogoTramites.tramites, "pasaporte")
        val porInstitucion = filtrarTramitesNotificaciones(CatalogoTramites.tramites, "ANT")

        assertEquals(listOf("pasaporte"), porTitulo.map { it.id })
        assertEquals(listOf("licencia"), porInstitucion.map { it.id })
    }

    @Test
    fun combinarFechaHora_respetaLaZonaIndicada() {
        val resultado = combinarFechaHora(
            fecha = LocalDate.of(2026, 8, 5),
            hora = 20,
            minuto = 15,
            zona = ZoneOffset.UTC
        )

        assertEquals(Instant.parse("2026-08-05T20:15:00Z").toEpochMilli(), resultado)
    }

    @Test
    fun seleccionarTramitesHistorial_respetaOrdenYDescartaIdsInvalidos() {
        val seleccionados = seleccionarTramitesHistorial(
            tramites = CatalogoTramites.tramites,
            idsHistorial = listOf("licencia", "cedula", "licencia", "id-inexistente")
        )

        assertEquals(listOf("licencia", "cedula"), seleccionados.map { it.id })
    }

    @Test
    fun calcularProgresoDetalle_limitaElResultadoEntreCeroYUno() {
        assertEquals(0f, calcularProgresoDetalle(-1, 5))
        assertEquals(0.4f, calcularProgresoDetalle(2, 5))
        assertEquals(1f, calcularProgresoDetalle(8, 5))
        assertEquals(0f, calcularProgresoDetalle(2, 0))
    }

    @Test
    fun construirTextoGuia_incluyeResumenYPasos() {
        val tramite = CatalogoTramites.tramites.first()
        val texto = construirTextoGuia(tramite)

        assertTrue(texto.contains(tramite.summary))
        assertTrue(tramite.steps.all { texto.contains(it.title) && texto.contains(it.textoAyuda) })
    }

    @Test
    fun limpiarHtmlGobEc_convierteContenidoALecturaSimple() {
        val html = "<p>Requisito&nbsp;uno</p><ol><li>C&eacute;dula vigente</li></ol>"

        val limpio = TextoHtmlGobEc.limpiar(html)

        assertFalse(limpio.contains("<p>"))
        assertTrue(limpio.contains("Requisito uno"))
        assertTrue(limpio.contains("Cédula vigente"))
    }

    @Test
    fun procedimientoGobEc_generaLaCantidadRealDePasosNumerados() {
        val procedimiento = """
            <p><strong>Procedimiento en línea:</strong></p>
            <p>1. Ingresar al portal<br>2. Iniciar sesión<br>3. Descargar el certificado</p>
        """.trimIndent()

        val pasos = TextoHtmlGobEc.extraerPasosProcedimiento(procedimiento)

        assertEquals(3, pasos.size)
        assertEquals("Ingresar al portal", pasos.first().descripcion)
        assertTrue(pasos.all { it.seccion == "Procedimiento en línea" })
    }

    @Test
    fun procedimientoGobEc_excluyePasosPresencialesCuandoExisteCanalVirtual() {
        val procedimiento = """
            <p><strong>Presencial:</strong></p><p>1. Acudir a una oficina</p>
            <p><strong>Línea:</strong></p><p>1. Abrir el portal<br>2. Descargar el resultado</p>
        """.trimIndent()

        val pasos = TextoHtmlGobEc.extraerPasosProcedimiento(procedimiento)

        assertEquals(listOf("Abrir el portal", "Descargar el resultado"), pasos.map { it.descripcion })
    }

    @Test
    fun selectorGobEc_descartaTramitesQueNoSonCompletamenteEnLinea() {
        val valido = tramiteGobEc("11745", "Emisión de Certificados y Actas Registrales")
        val incompleto = tramiteGobEc(
            id = "12371",
            nombre = "Adquisición de certificado de Firma Electrónica",
            completo = "No"
        )

        val seleccionados = SelectorTramitesGobEc.seleccionar(listOf(incompleto, valido))

        assertEquals(listOf("11745"), seleccionados.map { it.tramiteId })
    }

    @Test
    fun selectorGobEc_limitaAOchoTramitesPrioritarios() {
        val candidatos = listOf(
            tramiteGobEc("11745", "Emisión de Certificados y Actas Registrales", institucion = "23"),
            tramiteGobEc("11247", "Emisión de certificado de antecedentes penales", institucion = "588"),
            tramiteGobEc("11345", "Emisión de Certificado de Goce de Derechos Políticos", institucion = "413"),
            tramiteGobEc("1002", "Certificado de Registro Único de Contribuyente (RUC)", institucion = "8"),
            tramiteGobEc("15051", "Certificado de Autorización a terceros", institucion = "8"),
            tramiteGobEc("12781", "Certificado de afiliación al IESS", institucion = "163"),
            tramiteGobEc("12967", "Acceso a la información de historia laboral", institucion = "163"),
            tramiteGobEc("3718", "Apostilla y legalización de documentos", institucion = "6"),
            tramiteGobEc("4030", "Generación de claves SRI", institucion = "8")
        )

        val seleccionados = SelectorTramitesGobEc.seleccionar(candidatos)

        assertEquals(8, seleccionados.size)
        assertEquals("11745", seleccionados.first().tramiteId)
        assertFalse(seleccionados.map { it.tramiteId }.contains("4030"))
    }

    @Test
    fun tramiteGobEc_seMapeaConIdEstableEImagenOficial() {
        val remoto = tramiteGobEc(
            id = "1002",
            nombre = "Certificado de Registro Único de Contribuyente (RUC)",
            institucion = "8",
            institucionNombre = "Servicio de Rentas Internas",
            imagen = "https://www.gob.ec/sites/default/files/ruc.jpg",
            requisitos = "<p>Cédula o RUC</p>",
            procedimiento = "<p>1. Abrir SRI en línea<br>2. Descargar el certificado</p>",
            costo = "No",
            modificado = "<time datetime=\"2026-08-08T10:00:00-05:00\">fecha</time>"
        ).aTramite()

        assertEquals("gobec_1002", remoto.id)
        assertEquals("Servicio de Rentas Internas", remoto.institution)
        assertEquals("https://www.gob.ec/sites/default/files/ruc.jpg", remoto.imagenUrl)
        assertTrue(remoto.requisitosOficiales.orEmpty().contains("Cédula o RUC"))
        assertEquals(2, remoto.steps.size)
        assertEquals("No", remoto.costoOficial)
        assertEquals("2026-08-08", remoto.actualizadoEn)
    }

    @Test
    fun combinarCatalogos_conservaLocalesCuandoNoHayInternet() {
        val combinados = RepositorioCatalogoTramites.combinarCatalogos(
            locales = CatalogoTramites.tramites,
            remotos = emptyList()
        )

        assertEquals(CatalogoTramites.tramites.map { it.id }, combinados.map { it.id })
    }

    @Test
    fun combinarCatalogos_usaCatalogoOficialCuandoExisteCacheORed() {
        val remoto = tramiteGobEc("1002", "Certificado de Registro Único de Contribuyente (RUC)", institucion = "8").aTramite()

        val combinados = RepositorioCatalogoTramites.combinarCatalogos(
            locales = CatalogoTramites.tramites,
            remotos = listOf(remoto)
        )

        assertEquals(listOf("gobec_1002"), combinados.map { it.id })
    }

    @Test
    fun refrescoIncompleto_conservaElUltimoCacheOficial() {
        val cache = (1..8).map { indice ->
            tramiteGobEc(
                id = "cache_$indice",
                nombre = "Trámite oficial $indice"
            ).aTramite()
        }
        val remotoIncompleto = cache.take(3)

        val resultado = RepositorioCatalogoTramites.resolverCatalogoTrasRefresco(
            locales = CatalogoTramites.tramites,
            cache = cache,
            remotos = remotoIncompleto
        )

        assertEquals(cache.map { it.id }, resultado.map { it.id })
    }
}

private fun tramiteGobEc(
    id: String,
    nombre: String,
    institucion: String = "23",
    completo: String = "Sí",
    imagen: String = "https://www.gob.ec/sites/default/files/imagen.jpg",
    requisitos: String = "<p>Cédula vigente</p>",
    procedimiento: String = "<ol><li>Ingresar al portal.</li></ol>",
    costo: String = "No",
    costoDetalle: String = "",
    modificado: String = "2026-08-08",
    institucionNombre: String = ""
) = TramiteGobEcDto(
    tramiteId = id,
    nombre = nombre,
    url = "https://www.gob.ec/tramites/$id",
    institucionId = institucion,
    institucionUrl = "https://www.gob.ec/instituciones/$institucion",
    imagenUrl = imagen,
    descripcion = "<p>Descripción oficial del trámite.</p>",
    requisitosObligatorios = requisitos,
    requisitosEspeciales = "",
    procedimiento = procedimiento,
    tramiteEnLineaUrl = "https://www.gob.ec/tramites/$id/webform",
    tramiteEnLineaCompleto = completo,
    costo = costo,
    costoDetalle = costoDetalle,
    modificado = modificado,
    institucionNombre = institucionNombre
)


