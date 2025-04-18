package com.example.makeitmeme.navigation

import androidx.compose.runtime.*
import com.example.makeitmeme.ui.auth.AuthScreen
import com.example.makeitmeme.ui.main.HomeScreen
import com.example.makeitmeme.ui.main.MenuScreen
import com.example.makeitmeme.ui.main.ChatScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// Définition des écrans
enum class Screen {
    MENU, JEU, CHAT
}

@Composable
fun AuthNavigator() {
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
    var currentScreen by remember { mutableStateOf(Screen.MENU) }

    // Écoute des changements d'état de connexion
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        // Écran de connexion
        AuthScreen(
            auth = auth,
            onAuthComplete = { user -> currentUser = user }
        )
    } else {
        when (currentScreen) {
            Screen.MENU -> MenuScreen(
                user = currentUser!!,
                onPlay = { currentScreen = Screen.JEU },
                onChat = { currentScreen = Screen.CHAT },
                onLogout = {
                    auth.signOut()
                    currentUser = null
                }
            )

            Screen.JEU -> HomeScreen(
                user = currentUser!!,
                onBackToMenu = { currentScreen = Screen.MENU },
                onLogout = {
                    auth.signOut()
                    currentUser = null
                }
            )

            Screen.CHAT -> ChatScreen(
                onBackToMenu = { currentScreen = Screen.MENU }
            )
        }
    }
}
