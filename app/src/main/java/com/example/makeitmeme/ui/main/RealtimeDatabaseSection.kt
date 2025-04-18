package com.example.makeitmeme.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.makeitmeme.data.ChatMessage
import com.google.firebase.database.*

@Composable
fun RealtimeDatabaseSection() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }

    val database = FirebaseDatabase.getInstance()
    val messagesRef = database.reference.child("messages").child("public")

    // Listener Firebase
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                messages.clear()
                messages.addAll(newMessages)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        messagesRef.addValueEventListener(listener)
        onDispose {
            messagesRef.removeEventListener(listener)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("💬 Chat global :", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color(0xFFF1F1F1))
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                val username = message.sender.substringBefore("@")
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("👤 $username", style = MaterialTheme.typography.bodySmall)
                    Text(message.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Message") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val newMessage = ChatMessage(sender = "Anonyme", text = inputText)
                    messagesRef.push().setValue(newMessage)
                    inputText = ""
                }
            ) {
                Text("Envoyer")
            }
        }
    }
}
