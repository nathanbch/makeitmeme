package com.example.makeitmeme.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun MenuScreen(
    user: FirebaseUser,
    onPlay: () -> Unit,
    onChat: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenue ${user.email}", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) {
            Text("▶️ Jouer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onChat, modifier = Modifier.fillMaxWidth()) {
            Text("💬 Chat en ligne")
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = onLogout) {
            Text("Déconnexion")
        }
    }
}