package com.example.makeitmeme.ui.main

import android.graphics.*
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.makeitmeme.R
import com.example.makeitmeme.data.ChatMessage
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

@Composable
fun HomeScreen(user: FirebaseUser, onBackToMenu: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val memeImages = listOf(
        R.drawable.meme1, R.drawable.meme2, R.drawable.meme3,
        R.drawable.meme4, R.drawable.meme5, R.drawable.meme6,
        R.drawable.meme7, R.drawable.meme8, R.drawable.meme10
    )

    var randomImageRes by remember { mutableStateOf(memeImages.random()) }
    val memeBitmap = remember(randomImageRes) {
        try {
            val original = BitmapFactory.decodeResource(context.resources, randomImageRes)
            resizeBitmap(original, 1024)
        } catch (e: Exception) {
            null
        }
    }

    var topText by remember { mutableStateOf("") }
    var bottomText by remember { mutableStateOf("") }
    var changerCount by remember { mutableStateOf(0) }
    val maxChangerCount = 5
    var liked by remember { mutableStateOf(false) }
    var showHeartAnimation by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackToMenu) {
                Text("←", style = MaterialTheme.typography.titleLarge)
            }

            OutlinedButton(onClick = onLogout) {
                Text("Déconnexion")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Bienvenue ${user.email?.substringBefore("@") ?: "Utilisateur"}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            memeBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    liked = true
                                    showHeartAnimation = true
                                }
                            )
                        }
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Meme",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // ❤️ Animation
                LaunchedEffect(showHeartAnimation) {
                    if (showHeartAnimation) {
                        delay(1000)
                        showHeartAnimation = false
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showHeartAnimation,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text("❤️", style = MaterialTheme.typography.displayLarge)
                    }
                }
            } ?: Text("❌ Image introuvable", color = MaterialTheme.colorScheme.error)

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(topText, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                Text(bottomText, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (liked) {
            Text(
                text = "🧡 ${user.email?.substringBefore("@")} a liké cette image !",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = topText,
            onValueChange = { topText = it },
            label = { Text("Texte du haut") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bottomText,
            onValueChange = { bottomText = it },
            label = { Text("Texte du bas") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (changerCount < maxChangerCount) {
                        val filteredImages = memeImages.filterNot { it == randomImageRes }
                        if (filteredImages.isNotEmpty()) {
                            randomImageRes = filteredImages.random()
                            topText = ""
                            bottomText = ""
                            changerCount++
                            liked = false
                        } else {
                            Toast.makeText(context, "Aucune autre image disponible", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Limite atteinte : sauvegardez ou envoyez le mème pour réinitialiser", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = changerCount < maxChangerCount,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔁 Changer le mème ($changerCount/$maxChangerCount)")
            }

            Button(
                onClick = {
                    memeBitmap?.let { safeBitmap ->
                        val finalBitmap = generateMemeBitmap(safeBitmap, topText, bottomText)
                        val savedFile = saveBitmapToGallery(context, finalBitmap)
                        if (savedFile != null) {
                            changerCount = 0
                            Toast.makeText(context, "Image sauvegardée : ${savedFile.absolutePath}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
                        }
                    } ?: Toast.makeText(context, "Erreur : image introuvable", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📥 Générer / Sauvegarder le mème")
            }

            Button(
                onClick = {
                    memeBitmap?.let { safeBitmap ->
                        val finalBitmap = generateMemeBitmap(safeBitmap, topText, bottomText)
                        val savedFile = saveBitmapToGallery(context, finalBitmap)
                        if (savedFile != null) {
                            changerCount = 0
                            val publicRef = FirebaseDatabase.getInstance().reference.child("messages").child("public")
                            val message = ChatMessage(
                                sender = user.email ?: "inconnu",
                                text = "🖼️ Mème généré à partir de ${savedFile.name}"
                            )
                            publicRef.push().setValue(message)
                            Toast.makeText(context, "Mème envoyé dans le chat en ligne", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show()
                        }
                    } ?: Toast.makeText(context, "Erreur : image introuvable", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📨 Envoyer dans le chat en ligne")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        RealtimeDatabaseSection()
    }
}

// ✅ Redimensionne le bitmap pour éviter le crash
fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val scale = maxSize / maxOf(width, height).toFloat()
    val newWidth = (width * scale).toInt()
    val newHeight = (height * scale).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun generateMemeBitmap(baseBitmap: Bitmap, topText: String, bottomText: String): Bitmap {
    val result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        color = Color.WHITE
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 0f, 0f, Color.BLACK)
    }

    val x = canvas.width / 2f
    canvas.drawText(topText.uppercase(), x, 100f, paint)
    canvas.drawText(bottomText.uppercase(), x, canvas.height - 50f, paint)

    return result
}

fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap): File? {
    val filename = "meme_${System.currentTimeMillis()}.png"
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    if (!picturesDir.exists()) picturesDir.mkdirs()

    val file = File(picturesDir, filename)
    return try {
        val output = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
        output.close()

        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf("image/png"),
            null
        )

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
