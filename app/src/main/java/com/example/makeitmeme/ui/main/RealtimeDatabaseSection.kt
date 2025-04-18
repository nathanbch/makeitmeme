package com.example.makeitmeme.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import com.example.makeitmeme.data.ChatMessage


@Composable
fun RealtimeDatabaseSection() {
    val messageList = remember { mutableStateListOf<ChatMessage>() }
    val messageRef = FirebaseDatabase.getInstance()
        .getReference("messages")
        .child("public")

    val listener = remember {
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = snapshot.children.mapNotNull { msgSnap ->
                    try {
                        msgSnap.getValue(ChatMessage::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                messageList.clear()
                messageList.addAll(newMessages)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
    }

    LaunchedEffect(Unit) {
        messageRef.addValueEventListener(listener)
    }

    DisposableEffect(Unit) {
        onDispose {
            messageRef.removeEventListener(listener)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("📢 Chat Public", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        items(messageList) { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("👤 ${message.sender}", style = MaterialTheme.typography.bodySmall)
                    Text(message.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
