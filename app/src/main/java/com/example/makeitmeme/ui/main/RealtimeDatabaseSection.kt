package com.example.makeitmeme.ui.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val user: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun RealtimeDatabaseSection(
    listState: LazyListState = rememberLazyListState()
) {
    val messageRef = FirebaseDatabase.getInstance().getReference("messages")
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }

    var isSaving by remember { mutableStateOf(false) }
    var dbError by remember { mutableStateOf<String?>(null) }

    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Moi"
    val coroutineScope = rememberCoroutineScope()

    val dateFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    LaunchedEffect(messages.size) {
        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isMe = msg.user == currentUserEmail
                val timeFormatted = dateFormatter.format(Date(msg.timestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)
                    ) {
                        if (!isMe) {
                            Text(
                                text = msg.user,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = msg.text,
                            fontSize = 14.sp,
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Écris ton message") }
            )
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (messageText.isBlank()) return@Button
                    isSaving = true
                    dbError = null
                    coroutineScope.launch {
                        val message = ChatMessage(
                            user = currentUserEmail,
                            text = messageText,
                            timestamp = System.currentTimeMillis()
                        )
                        try {
                            messageRef.push().setValue(message).await()
                            messageText = ""
                            Log.d("RTDBSection", "Message saved successfully")
                        } catch (e: Exception) {
                            dbError = "Erreur sauvegarde: ${e.localizedMessage}"
                            Log.e("RTDBSection", "Firebase error", e)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Envoyer")
                }
            }
        }

        if (dbError != null) {
            Text(
                text = dbError ?: "",
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    // Écoute les nouveaux messages en temps réel
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val msgList = mutableListOf<ChatMessage>()
                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        msgList.add(msg)
                    }
                }
                msgList.sortBy { it.timestamp }
                messages = msgList
            }

            override fun onCancelled(error: DatabaseError) {
                dbError = "Erreur Firebase: ${error.message}"
                Log.e("RTDBSection", "onCancelled: ${error.message}")
            }
        }

        messageRef.addValueEventListener(listener)
        onDispose {
            messageRef.removeEventListener(listener)
        }
    }
}
