package com.example.makeitmeme.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

data class LikeMessage(
    val user: String = "",
    val timestamp: Long = 0L
)

@Composable
fun RealtimeDatabaseSection() {
    var likes by remember { mutableStateOf(listOf<LikeMessage>()) }

    DisposableEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("likes")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likeList = snapshot.children.mapNotNull { it.getValue(LikeMessage::class.java) }
                    .sortedByDescending { it.timestamp }
                likes = likeList
            }

            override fun onCancelled(error: DatabaseError) {
                // Gérer les erreurs si nécessaire
            }
        }

        ref.addValueEventListener(listener)

        onDispose {
            ref.removeEventListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text("💬 Réactions :", style = MaterialTheme.typography.titleMedium)

        if (likes.isEmpty()) {
            Text("Aucune réaction pour le moment.")
        } else {
            likes.forEach { like ->
                val name = like.user.substringBefore("@")
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(like.timestamp))

                Text("🧡 $name a liké à $time")
            }
        }
    }
}
