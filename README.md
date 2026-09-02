# AsistenTED

Aplicación móvil Android que orienta a ciudadanos ecuatorianos en la consulta y organización de trámites en línea, utilizando información oficial de Gob.Ec.

> AsistenTED guía al usuario: no realiza el trámite directamente. Cuando la persona está lista, la aplicación la dirige al portal oficial correspondiente.

## Características

- Consulta de un catálogo de trámites oficiales de Gob.Ec.
- Búsqueda y filtrado por institución.
- Información de cada trámite: descripción, requisitos, costo y procedimiento.
- Pasos variables según la información oficial disponible.
- Acceso directo al portal oficial del trámite.
- Registro, inicio de sesión y acceso como invitado.
- Favoritos, historial de consultas y seguimiento de progreso.
- Recordatorios locales asociados a trámites.
- Perfil editable con avatares.
- Opciones de accesibilidad: texto grande, alto contraste y lectura en voz alta.
- Foro por trámite: publicar, responder, editar y eliminar comentarios propios.

## Tecnologías

- Kotlin
- Android Studio
- Jetpack Compose y Material 3
- Navigation Compose
- Firebase Authentication
- Cloud Firestore
- API pública de Gob.Ec
- Coil para carga de imágenes
- JUnit y pruebas instrumentadas de Android

## Arquitectura

```text
MainActivity
   ↓
ControladorAsistenTed
   ↓
Pantallas Jetpack Compose
   ↓
Repositorios
   ├── Firebase Authentication y Firestore
   ├── API de Gob.Ec
   ├── Preferencias locales
   └── Notificaciones Android
