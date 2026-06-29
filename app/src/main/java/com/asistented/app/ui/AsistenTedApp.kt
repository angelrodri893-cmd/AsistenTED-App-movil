package com.asistented.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.asistented.app.data.AuthRepository
import com.asistented.app.data.ForumRepository
import com.asistented.app.data.LocalPreferences
import com.asistented.app.data.ProcedureCatalog
import com.asistented.app.data.UserRepository
import com.asistented.app.data.model.AccessibilitySettings
import com.asistented.app.data.model.ForumComment
import com.asistented.app.data.model.HistoryItem
import com.asistented.app.data.model.Procedure
import com.asistented.app.data.model.Reminder
import com.asistented.app.data.model.UserProfile
import com.asistented.app.domain.AuthRules
import com.asistented.app.domain.ProgressRules
import com.asistented.app.domain.UserContentRules
import com.asistented.app.notifications.ReminderScheduler
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.UUID

class AsistenTedController(context: Context) {
    private val appContext = context.applicationContext
    private val localPreferences = LocalPreferences(appContext)
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val forumRepository = ForumRepository()
    private val reminderScheduler = ReminderScheduler(appContext)

    val procedures = ProcedureCatalog.procedures
    val favorites = mutableStateListOf<String>()
    val history = mutableStateListOf<HistoryItem>()
    val reminders = mutableStateListOf<Reminder>()
    val comments = mutableStateMapOf<String, List<ForumComment>>()
    val completedSteps = mutableStateMapOf<String, Set<String>>()

    var currentUser by mutableStateOf<UserProfile?>(null)
        private set
    var accessibilitySettings by mutableStateOf(localPreferences.loadAccessibility())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    init {
        authRepository.currentUser { profile ->
            profile?.let { setSignedUser(it) }
        }
    }

    fun clearMessage() {
        message = null
    }

    fun showMessage(text: String) {
        message = text
    }

    fun enterAsGuest() {
        currentUser = authRepository.guestProfile().copy(accessibility = accessibilitySettings)
        favorites.clear()
        history.clear()
        reminders.clear()
        message = "Entraste como anónimo. Puedes ver guías, pero algunas funciones se guardan solo con cuenta."
    }

    fun register(username: String, firstName: String, lastName: String, password: String, confirmation: String) {
        val usernameError = AuthRules.validateUsername(username)
        val passwordError = AuthRules.validatePassword(password, confirmation)
        if (usernameError != null || passwordError != null) {
            message = usernameError ?: passwordError
            return
        }
        isLoading = true
        authRepository.register(username, firstName, lastName, password) { result ->
            isLoading = false
            result
                .onSuccess {
                    setSignedUser(it)
                    message = "Cuenta creada correctamente."
                }
                .onFailure { message = it.asUserMessage("No se pudo crear la cuenta.") }
        }
    }

    fun login(username: String, password: String) {
        val usernameError = AuthRules.validateUsername(username)
        val passwordError = AuthRules.validatePassword(password)
        if (usernameError != null || passwordError != null) {
            message = usernameError ?: passwordError
            return
        }
        isLoading = true
        authRepository.login(username, password) { result ->
            isLoading = false
            result
                .onSuccess {
                    setSignedUser(it)
                    message = "Sesión iniciada."
                }
                .onFailure { message = it.asUserMessage("Usuario o contraseña incorrectos.") }
        }
    }

    fun signOut() {
        authRepository.signOut()
        currentUser = null
        favorites.clear()
        history.clear()
        reminders.clear()
        completedSteps.clear()
        comments.clear()
    }

    fun updateAccessibility(settings: AccessibilitySettings) {
        accessibilitySettings = settings
        localPreferences.saveAccessibility(settings)
        currentUser = currentUser?.copy(accessibility = settings)
        currentUser?.takeIf { !it.isGuest }?.let { userRepository.saveProfile(it) }
    }

    fun updateProfile(firstName: String, lastName: String, avatarId: String) {
        val user = currentUser ?: return
        if (user.isGuest) {
            message = "Crea una cuenta para guardar tu perfil."
            return
        }
        val updated = user.copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            avatarId = avatarId
        )
        currentUser = updated
        userRepository.saveProfile(updated) {
            message = if (it.isSuccess) "Perfil guardado." else "No se pudo guardar el perfil."
        }
    }

    fun loadComments(procedureId: String) {
        forumRepository.load(procedureId) { loaded ->
            if (loaded.isNotEmpty()) {
                comments[procedureId] = loaded
            }
        }
    }

    fun markConsulted(procedureId: String) {
        val updated = UserContentRules.recordHistory(history, procedureId, System.currentTimeMillis())
        history.clear()
        history.addAll(updated)
        val item = updated.first()
        currentUser?.let { userRepository.addHistory(it.uid, item) }
    }

    fun toggleFavorite(procedureId: String) {
        val user = currentUser ?: return
        if (user.isGuest) {
            message = "Inicia sesión para guardar favoritos."
            return
        }
        val updated = UserContentRules.toggleFavorite(favorites, procedureId)
        val enabled = updated.contains(procedureId)
        favorites.clear()
        favorites.addAll(updated)
        userRepository.saveFavorite(user.uid, procedureId, enabled)
    }

    fun toggleStep(procedureId: String, stepId: String) {
        val current = completedSteps[procedureId].orEmpty()
        val updated = ProgressRules.toggleStep(current, stepId)
        completedSteps[procedureId] = updated
        currentUser?.let { userRepository.saveProgress(it.uid, procedureId, updated) }
    }

    fun addComment(procedureId: String, text: String) {
        val user = currentUser ?: return
        if (user.isGuest) {
            message = "Inicia sesión para comentar en el foro."
            return
        }
        if (text.isBlank()) {
            message = "Escribe tu pregunta o comentario."
            return
        }
        val comment = ForumComment(
            id = UUID.randomUUID().toString(),
            procedureId = procedureId,
            userId = user.uid,
            username = user.displayName,
            text = text.trim(),
            createdAtMillis = System.currentTimeMillis()
        )
        comments[procedureId] = comments[procedureId].orEmpty() + comment
        forumRepository.publish(comment)
        message = "Comentario publicado."
    }

    fun addReminder(procedureId: String, title: String, notes: String, scheduledAtMillis: Long) {
        val user = currentUser ?: return
        if (user.isGuest) {
            message = "Inicia sesión para guardar recordatorios."
            return
        }
        if (scheduledAtMillis <= System.currentTimeMillis()) {
            message = "Elige una fecha y hora futura."
            return
        }
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            procedureId = procedureId,
            title = title.ifBlank { "Recordatorio de trámite" },
            notes = notes,
            scheduledAtMillis = scheduledAtMillis
        )
        reminders.add(0, reminder)
        userRepository.saveReminder(user.uid, reminder)
        reminderScheduler.schedule(reminder)
        message = "Recordatorio guardado."
    }

    fun deleteReminder(reminder: Reminder) {
        reminders.remove(reminder)
        reminderScheduler.cancel(reminder)
        currentUser?.let { userRepository.deleteReminder(it.uid, reminder.id) }
    }

    private fun setSignedUser(profile: UserProfile) {
        val signedUser = profile.copy(accessibility = accessibilitySettings)
        currentUser = signedUser
        loadPersistedUserData(signedUser.uid)
    }

    private fun loadPersistedUserData(uid: String) {
        userRepository.loadFavorites(uid) { loaded ->
            favorites.clear()
            favorites.addAll(loaded)
        }
        userRepository.loadHistory(uid) { loaded ->
            history.clear()
            history.addAll(loaded)
        }
        userRepository.loadProgress(uid) { loaded ->
            completedSteps.clear()
            completedSteps.putAll(loaded)
        }
        userRepository.loadReminders(uid) { loaded ->
            reminders.clear()
            reminders.addAll(loaded)
        }
    }
}

private fun Throwable.asUserMessage(defaultMessage: String): String {
    val raw = (localizedMessage ?: message).orEmpty()
    return when {
        raw.contains("PERMISSION_DENIED", ignoreCase = true) ||
            raw.contains("permission_denied", ignoreCase = true) ||
            raw.contains("Missing or insufficient permissions", ignoreCase = true) ->
            "Faltan permisos en Firestore. Publica las reglas de seguridad del proyecto y vuelve a intentar."
        else -> raw.ifBlank { defaultMessage }
    }
}

@Composable
fun AsistenTedApp(controller: AsistenTedController) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller.message) {
        val current = controller.message
        if (current != null) {
            scope.launch { snackbarHostState.showSnackbar(current) }
            controller.clearMessage()
        }
    }

    if (controller.currentUser == null) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            AuthScreen(
                controller = controller,
                modifier = Modifier.padding(padding)
            )
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { MainBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(controller, navController)
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("procedureId") { type = NavType.StringType })
            ) { entry ->
                val procedureId = entry.arguments?.getString("procedureId").orEmpty()
                ProcedureCatalog.findProcedure(procedureId)?.let {
                    ProcedureDetailScreen(controller, it, navController)
                }
            }
            composable(Routes.FAVORITES) {
                ProcedureListScreen(
                    title = "Favoritos",
                    emptyText = "Todavía no guardas trámites favoritos.",
                    procedures = controller.procedures.filter { controller.favorites.contains(it.id) },
                    controller = controller,
                    navController = navController
                )
            }
            composable(Routes.HISTORY) {
                val historyProcedures = controller.history.mapNotNull { ProcedureCatalog.findProcedure(it.procedureId) }
                ProcedureListScreen(
                    title = "Historial",
                    emptyText = "Aquí aparecerán los trámites que revises.",
                    procedures = historyProcedures,
                    controller = controller,
                    navController = navController
                )
            }
            composable(Routes.REMINDERS) {
                RemindersScreen(controller)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(controller)
            }
            composable(Routes.ACCESSIBILITY) {
                AccessibilityScreen(controller)
            }
        }
    }
}

@Composable
private fun AuthScreen(controller: AsistenTedController, modifier: Modifier = Modifier) {
    var registerMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("AsistenTED", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(
            "Guía sencilla para trámites digitales del Ecuador.",
            style = readableBody(controller)
        )
        NoticeCard(
            title = "Puedes entrar sin cuenta",
            text = "Como anónimo podrás revisar las guías. Para guardar favoritos, historial, perfil, foro y recordatorios necesitas registrarte."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !registerMode, onClick = { registerMode = false }, label = { Text("Iniciar sesión") })
            FilterChip(selected = registerMode, onClick = { registerMode = true }, label = { Text("Registrarme") })
        }
        if (registerMode) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Nombre") },
                supportingText = { Text("Escribe cómo quieres que te saludemos.") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            supportingText = { Text("No necesitas correo ni número de teléfono.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = { Text("Mínimo 6 caracteres.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (registerMode) {
            OutlinedTextField(
                value = confirmation,
                onValueChange = { confirmation = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            onClick = {
                if (registerMode) {
                    controller.register(username, firstName, lastName, password, confirmation)
                } else {
                    controller.login(username, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = !controller.isLoading
        ) {
            if (controller.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(if (registerMode) "Crear cuenta" else "Entrar")
            }
        }
        OutlinedButton(
            onClick = controller::enterAsGuest,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Continuar como anónimo")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(controller: AsistenTedController, navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var selectedInstitution by remember { mutableStateOf("Todas") }
    val institutions = listOf("Todas") + controller.procedures.map { it.institution }.distinct()
    val filtered = controller.procedures.filter {
        (selectedInstitution == "Todas" || it.institution == selectedInstitution) &&
            (query.isBlank() || it.title.contains(query, ignoreCase = true) || it.institution.contains(query, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${controller.currentUser?.displayName ?: "Anónimo"}", fontWeight = FontWeight.Bold)
                        Text("Elige un trámite para guiarte paso a paso.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar trámite") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    supportingText = { Text("Puedes buscar por nombre o institución.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    institutions.take(3).forEach { institution ->
                        FilterChip(
                            selected = selectedInstitution == institution,
                            onClick = { selectedInstitution = institution },
                            label = { Text(institution, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
            items(filtered, key = { it.id }) { procedure ->
                ProcedureCard(
                    procedure = procedure,
                    isFavorite = controller.favorites.contains(procedure.id),
                    onFavorite = { controller.toggleFavorite(procedure.id) },
                    onOpen = {
                        controller.markConsulted(procedure.id)
                        navController.navigate(Routes.detail(procedure.id))
                    },
                    largeText = controller.accessibilitySettings.largeText
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcedureDetailScreen(
    controller: AsistenTedController,
    procedure: Procedure,
    navController: NavHostController
) {
    val context = LocalContext.current
    val speaker = rememberGuideSpeaker()
    var commentText by remember { mutableStateOf("") }
    val completed = controller.completedSteps[procedure.id].orEmpty()
    val fullText = buildString {
        append(procedure.title).append(". ")
        procedure.steps.forEach { step ->
            append(step.title).append(". ")
            append(step.description).append(". ")
            append(step.helpText).append(". ")
        }
    }

    LaunchedEffect(procedure.id) {
        controller.loadComments(procedure.id)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(procedure.institution) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { controller.toggleFavorite(procedure.id) }) {
                        Icon(
                            if (controller.favorites.contains(procedure.id)) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorito"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(procedure.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(procedure.summary, style = readableBody(controller))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(procedure.officialUrl)))
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Portal oficial")
                    }
                    OutlinedButton(
                        onClick = {
                            if (speaker.isSpeaking) speaker.stop() else speaker.speak(fullText)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(if (speaker.isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (speaker.isSpeaking) "Detener" else "Escuchar")
                    }
                }
            }
            items(procedure.steps, key = { it.id }) { step ->
                GuideStepCard(
                    title = step.title,
                    description = step.description,
                    helpText = step.helpText,
                    imagePlaceholder = step.imagePlaceholder,
                    checkItems = step.checkItems,
                    completed = completed.contains(step.id),
                    onCompletedChange = { controller.toggleStep(procedure.id, step.id) },
                    largeText = controller.accessibilitySettings.largeText
                )
            }
            item {
                ForumSection(
                    controller = controller,
                    procedure = procedure,
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    onPublish = {
                        controller.addComment(procedure.id, commentText)
                        commentText = ""
                    }
                )
            }
        }
    }
}

@Composable
private fun ProcedureListScreen(
    title: String,
    emptyText: String,
    procedures: List<Procedure>,
    controller: AsistenTedController,
    navController: NavHostController
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (procedures.isEmpty()) {
            NoticeCard(title = "Sin datos", text = emptyText)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(procedures, key = { it.id }) { procedure ->
                    ProcedureCard(
                        procedure = procedure,
                        isFavorite = controller.favorites.contains(procedure.id),
                        onFavorite = { controller.toggleFavorite(procedure.id) },
                        onOpen = {
                            controller.markConsulted(procedure.id)
                            navController.navigate(Routes.detail(procedure.id))
                        },
                        largeText = controller.accessibilitySettings.largeText
                    )
                }
            }
        }
    }
}

@Composable
private fun RemindersScreen(controller: AsistenTedController) {
    val context = LocalContext.current
    var selectedProcedureId by remember { mutableStateOf(controller.procedures.first().id) }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var time by remember { mutableStateOf("09:00") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Recordatorios", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Agenda una fecha para volver a revisar un trámite.", style = readableBody(controller))
        }
        if (controller.currentUser?.isGuest == true) {
            item {
                NoticeCard("Cuenta necesaria", "Para guardar recordatorios y recibir avisos necesitas iniciar sesión.")
            }
        } else {
            item {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Nuevo recordatorio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        ProcedureSelector(controller.procedures, selectedProcedureId) { selectedProcedureId = it }
                        OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(notes, { notes = it }, label = { Text("Nota de ayuda") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(date, { date = it }, label = { Text("Fecha") }, supportingText = { Text("AAAA-MM-DD") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(time, { time = it }, label = { Text("Hora") }, supportingText = { Text("HH:MM") }, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                val millis = parseDateTime(date, time)
                                if (millis == null) {
                                    controller.showMessage("Usa una fecha y hora válidas.")
                                } else {
                                    controller.addReminder(selectedProcedureId, title, notes, millis)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Guardar recordatorio")
                        }
                    }
                }
            }
        }
        items(controller.reminders, key = { it.id }) { reminder ->
            ReminderCard(reminder, controller)
        }
    }
}

@Composable
private fun ProfileScreen(controller: AsistenTedController) {
    val user = controller.currentUser
    var firstName by remember(user?.uid) { mutableStateOf(user?.firstName.orEmpty()) }
    var lastName by remember(user?.uid) { mutableStateOf(user?.lastName.orEmpty()) }
    var selectedAvatarId by remember(user?.uid) { mutableStateOf(user?.avatarId ?: ProfileAvatars.defaultId) }
    val selectedAvatar = ProfileAvatars.find(selectedAvatarId)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (user?.isGuest == true) {
            NoticeCard("Estás como anónimo", "Puedes consultar guías. Para guardar perfil, favoritos, historial y foro, crea una cuenta.")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AvatarPreview(avatar = selectedAvatar, size = 76)
            Column {
                Text(user?.displayName.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Avatar preestablecido: ${selectedAvatar.label}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedTextField(value = user?.username.orEmpty(), onValueChange = {}, label = { Text("Usuario") }, enabled = false, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(firstName, { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(lastName, { lastName = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
        AvatarSelector(
            selectedAvatarId = selectedAvatarId,
            onSelected = { selectedAvatarId = it }
        )
        NoticeCard("Foto de perfil", "Elige una imagen preestablecida. La app no sube fotos personales ni usa almacenamiento en la nube.")
        Button(
            onClick = { controller.updateProfile(firstName, lastName, selectedAvatarId) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Guardar cambios")
        }
        OutlinedButton(
            onClick = controller::signOut,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun AvatarSelector(
    selectedAvatarId: String,
    onSelected: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Selecciona tu avatar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Son imágenes locales de la app. No se sube ningún archivo personal.",
                style = MaterialTheme.typography.bodyMedium
            )
            ProfileAvatars.options.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { avatar ->
                        AvatarOptionCard(
                            avatar = avatar,
                            selected = avatar.id == selectedAvatarId,
                            onClick = { onSelected(avatar.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarOptionCard(
    avatar: ProfileAvatar,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AvatarPreview(avatar = avatar, size = 52)
            Text(avatar.label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun AvatarPreview(avatar: ProfileAvatar, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(avatar.background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            avatar.initials,
            color = avatar.foreground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class ProfileAvatar(
    val id: String,
    val label: String,
    val initials: String,
    val background: Color,
    val foreground: Color
)

private object ProfileAvatars {
    const val defaultId = "azul"

    val options = listOf(
        ProfileAvatar("azul", "Azul", "AZ", Color(0xFFDDEBFF), Color(0xFF204D85)),
        ProfileAvatar("amarillo", "Amarillo", "AM", Color(0xFFFFF4C2), Color(0xFF6E5200)),
        ProfileAvatar("rojo", "Rojo", "RJ", Color(0xFFFFE1E1), Color(0xFF8B2E2E)),
        ProfileAvatar("verde", "Verde", "VE", Color(0xFFDFF5E3), Color(0xFF236336)),
        ProfileAvatar("celeste", "Celeste", "CE", Color(0xFFD8F3FF), Color(0xFF1E5C70)),
        ProfileAvatar("violeta", "Violeta", "VI", Color(0xFFEDE4FF), Color(0xFF59408C))
    )

    fun find(id: String): ProfileAvatar = options.firstOrNull { it.id == id } ?: options.first()
}

@Composable
private fun AccessibilityScreen(controller: AsistenTedController) {
    val settings = controller.accessibilitySettings
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Accesibilidad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Ajusta la lectura visual de toda la app.", style = readableBody(controller))
        SettingRow(
            title = "Texto grande",
            description = "Aumenta el tamaño de explicaciones, ayudas y pasos.",
            checked = settings.largeText,
            onCheckedChange = { controller.updateAccessibility(settings.copy(largeText = it)) }
        )
        SettingRow(
            title = "Alto contraste",
            description = "Usa fondo oscuro y colores más fuertes para leer mejor.",
            checked = settings.highContrast,
            onCheckedChange = { controller.updateAccessibility(settings.copy(highContrast = it)) }
        )
    }
}

@Composable
private fun ProcedureCard(
    procedure: Procedure,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onOpen: () -> Unit,
    largeText: Boolean
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(procedure.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(procedure.institution, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onFavorite) {
                    Icon(if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Favorito")
                }
            }
            Text(procedure.summary, style = if (largeText) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(procedure.category) })
                AssistChip(onClick = {}, label = { Text("${procedure.steps.size} pasos") })
            }
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Ver guía")
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    title: String,
    description: String,
    helpText: String,
    imagePlaceholder: String,
    checkItems: List<String>,
    completed: Boolean,
    onCompletedChange: () -> Unit,
    largeText: Boolean
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = completed, onCheckedChange = { onCompletedChange() })
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Text(description, style = if (largeText) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
            NoticeCard("Ayuda", helpText)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(imagePlaceholder, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
            }
            checkItems.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ForumSection(
    controller: AsistenTedController,
    procedure: Procedure,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onPublish: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Forum, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Foro del trámite", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (controller.currentUser?.isGuest == true) {
            NoticeCard("Solo lectura", "Inicia sesión para publicar preguntas o comentarios.")
        } else {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                label = { Text("Pregunta o comentario") },
                supportingText = { Text("Escribe con respeto y evita datos personales sensibles.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(onClick = onPublish, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Publicar")
            }
        }
        val comments = controller.comments[procedure.id].orEmpty()
        if (comments.isEmpty()) {
            Text("Aún no hay comentarios para este trámite.", style = MaterialTheme.typography.bodyMedium)
        } else {
            comments.forEach { comment ->
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(comment.username, fontWeight = FontWeight.Bold)
                        Text(comment.text)
                        Text(formatDate(comment.createdAtMillis), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcedureSelector(procedures: List<Procedure>, selectedId: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Trámite relacionado", style = MaterialTheme.typography.labelLarge)
        procedures.take(6).forEach { procedure ->
            FilterChip(
                selected = selectedId == procedure.id,
                onClick = { onSelected(procedure.id) },
                label = { Text(procedure.title) }
            )
        }
    }
}

@Composable
private fun ReminderCard(reminder: Reminder, controller: AsistenTedController) {
    val procedure = ProcedureCatalog.findProcedure(reminder.procedureId)
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(reminder.title, fontWeight = FontWeight.Bold)
                Text(procedure?.title ?: "Trámite", style = MaterialTheme.typography.bodyMedium)
                Text(formatDate(reminder.scheduledAtMillis), style = MaterialTheme.typography.labelMedium)
                if (reminder.notes.isNotBlank()) Text(reminder.notes, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { controller.deleteReminder(reminder) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun NoticeCard(title: String, text: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MainBottomBar(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination?.route.orEmpty()
    val items = listOf(
        BottomItem(Routes.HOME, "Inicio", Icons.Default.Home),
        BottomItem(Routes.FAVORITES, "Favoritos", Icons.Default.Bookmark),
        BottomItem(Routes.HISTORY, "Historial", Icons.Default.History),
        BottomItem(Routes.REMINDERS, "Avisos", Icons.Default.Notifications),
        BottomItem(Routes.PROFILE, "Perfil", Icons.Default.AccountCircle),
        BottomItem(Routes.ACCESSIBILITY, "Acceso", Icons.Default.Accessibility)
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, maxLines = 1) }
            )
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val REMINDERS = "reminders"
    const val PROFILE = "profile"
    const val ACCESSIBILITY = "accessibility"
    const val DETAIL = "procedure/{procedureId}"

    fun detail(procedureId: String) = "procedure/$procedureId"
}

@Composable
private fun readableBody(controller: AsistenTedController) =
    if (controller.accessibilitySettings.largeText) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge

@Composable
private fun rememberGuideSpeaker(): GuideSpeaker {
    val context = LocalContext.current
    val speaker = remember { GuideSpeaker(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { speaker.shutdown() }
    }
    return speaker
}

class GuideSpeaker(context: Context) : TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(context, this)
    private var ready by mutableStateOf(false)
    var isSpeaking by mutableStateOf(false)
        private set

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            textToSpeech.language = Locale.Builder().setLanguage("es").setRegion("EC").build()
            textToSpeech.setSpeechRate(0.88f)
        }
    }

    fun speak(text: String) {
        if (!ready) return
        isSpeaking = true
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "guide")
    }

    fun stop() {
        textToSpeech.stop()
        isSpeaking = false
    }

    fun shutdown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}

private fun parseDateTime(date: String, time: String): Long? {
    return try {
        LocalDateTime.parse("$date $time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun formatDate(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
