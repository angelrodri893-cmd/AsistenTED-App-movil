package com.asistented.app.data

import com.asistented.app.data.model.AccessibilitySettings
import com.asistented.app.data.model.ForumComment
import com.asistented.app.data.model.HistoryItem
import com.asistented.app.data.model.Reminder
import com.asistented.app.data.model.UserProfile
import com.asistented.app.domain.AuthRules
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository()
) {
    fun register(
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        onResult: (Result<UserProfile>) -> Unit
    ) {
        val normalized = AuthRules.normalizeUsername(username)
        auth.createUserWithEmailAndPassword(AuthRules.usernameToInternalEmail(normalized), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid.orEmpty()
                val profile = UserProfile(
                    uid = uid,
                    username = normalized,
                    firstName = firstName.trim(),
                    lastName = lastName.trim()
                )
                userRepository.saveProfile(profile) { saveResult ->
                    onResult(saveResult.map { profile })
                }
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }

    fun login(username: String, password: String, onResult: (Result<UserProfile>) -> Unit) {
        val normalized = AuthRules.normalizeUsername(username)
        auth.signInWithEmailAndPassword(AuthRules.usernameToInternalEmail(normalized), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid.orEmpty()
                userRepository.loadProfile(uid) { profileResult ->
                    onResult(profileResult.map { profile ->
                        profile.copy(username = profile.username.ifBlank { normalized })
                    })
                }
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }

    fun currentUser(onResult: (UserProfile?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(null)
            return
        }
        userRepository.loadProfile(uid) { result ->
            onResult(result.getOrNull())
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun guestProfile(): UserProfile = UserProfile(
        uid = "guest",
        username = "anonimo",
        firstName = "Anónimo",
        lastName = "",
        isGuest = true
    )
}

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun saveProfile(profile: UserProfile, onResult: (Result<Unit>) -> Unit = {}) {
        val data = mapOf(
            "uid" to profile.uid,
            "username" to profile.username,
            "firstName" to profile.firstName,
            "lastName" to profile.lastName,
            "avatarId" to profile.avatarId,
            "isGuest" to profile.isGuest,
            "largeText" to profile.accessibility.largeText,
            "highContrast" to profile.accessibility.highContrast
        )
        db.collection("users").document(profile.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun loadProfile(uid: String, onResult: (Result<UserProfile>) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(Result.failure(IllegalStateException("No se encontró el perfil.")))
                    return@addOnSuccessListener
                }
                onResult(
                    Result.success(
                        UserProfile(
                            uid = uid,
                            username = document.getString("username").orEmpty(),
                            firstName = document.getString("firstName").orEmpty(),
                            lastName = document.getString("lastName").orEmpty(),
                            avatarId = document.getString("avatarId").orEmpty().ifBlank { "azul" },
                            isGuest = document.getBoolean("isGuest") ?: false,
                            accessibility = AccessibilitySettings(
                                largeText = document.getBoolean("largeText") ?: false,
                                highContrast = document.getBoolean("highContrast") ?: false
                            )
                        )
                    )
                )
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun saveFavorite(uid: String, procedureId: String, enabled: Boolean) {
        if (uid == "guest") return
        val operation = if (enabled) {
            FieldValue.arrayUnion(procedureId)
        } else {
            FieldValue.arrayRemove(procedureId)
        }
        db.collection("users").document(uid).set(mapOf("favorites" to operation), SetOptions.merge())
    }

    fun loadFavorites(uid: String, onResult: (List<String>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyList())
            return
        }
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val favorites = document.get("favorites") as? List<*>
                onResult(favorites.orEmpty().filterIsInstance<String>())
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun saveProgress(uid: String, procedureId: String, completedStepIds: Set<String>) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("progress")
            .document(procedureId)
            .set(
                mapOf(
                    "completedSteps" to completedStepIds.toList(),
                    "updatedAtMillis" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
    }

    fun loadProgress(uid: String, onResult: (Map<String, Set<String>>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyMap())
            return
        }
        db.collection("users").document(uid)
            .collection("progress")
            .get()
            .addOnSuccessListener { snapshot ->
                val progress = snapshot.documents.associate { document ->
                    val steps = document.get("completedSteps") as? List<*>
                    document.id to steps.orEmpty().filterIsInstance<String>().toSet()
                }
                onResult(progress)
            }
            .addOnFailureListener { onResult(emptyMap()) }
    }

    fun addHistory(uid: String, item: HistoryItem) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("history")
            .document("${item.procedureId}_${item.consultedAtMillis}")
            .set(item)
    }

    fun loadHistory(uid: String, onResult: (List<HistoryItem>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyList())
            return
        }
        db.collection("users").document(uid)
            .collection("history")
            .orderBy("consultedAtMillis", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents.mapNotNull { document ->
                        val procedureId = document.getString("procedureId") ?: return@mapNotNull null
                        val consultedAt = document.getLong("consultedAtMillis") ?: return@mapNotNull null
                        HistoryItem(procedureId, consultedAt)
                    }
                )
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun saveReminder(uid: String, reminder: Reminder) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("reminders")
            .document(reminder.id)
            .set(reminder, SetOptions.merge())
    }

    fun loadReminders(uid: String, onResult: (List<Reminder>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyList())
            return
        }
        db.collection("users").document(uid)
            .collection("reminders")
            .orderBy("scheduledAtMillis", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents.mapNotNull { document ->
                        val id = document.getString("id") ?: document.id
                        val procedureId = document.getString("procedureId") ?: return@mapNotNull null
                        val title = document.getString("title") ?: return@mapNotNull null
                        val notes = document.getString("notes").orEmpty()
                        val scheduledAt = document.getLong("scheduledAtMillis") ?: return@mapNotNull null
                        Reminder(id, procedureId, title, notes, scheduledAt)
                    }
                )
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun deleteReminder(uid: String, reminderId: String) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("reminders")
            .document(reminderId)
            .delete()
    }
}

class ForumRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun publish(comment: ForumComment, onResult: (Result<Unit>) -> Unit = {}) {
        db.collection("procedures")
            .document(comment.procedureId)
            .collection("comments")
            .document(comment.id)
            .set(comment)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun load(procedureId: String, onResult: (List<ForumComment>) -> Unit) {
        db.collection("procedures")
            .document(procedureId)
            .collection("comments")
            .orderBy("createdAtMillis", Query.Direction.ASCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents.mapNotNull { document ->
                        val id = document.getString("id") ?: document.id
                        val userId = document.getString("userId") ?: return@mapNotNull null
                        val username = document.getString("username") ?: "Usuario"
                        val text = document.getString("text") ?: return@mapNotNull null
                        val createdAt = document.getLong("createdAtMillis") ?: return@mapNotNull null
                        ForumComment(id, procedureId, userId, username, text, createdAt)
                    }
                )
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}
