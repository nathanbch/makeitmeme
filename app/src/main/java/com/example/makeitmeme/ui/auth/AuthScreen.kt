package com.example.makeitmeme.ui.auth

import com.example.makeitmeme.R
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(auth: FirebaseAuth, onAuthComplete: (FirebaseUser) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            coroutineScope.launch {
                try {
                    val user = auth.signInWithCredential(credential).await().user
                    user?.let { onAuthComplete(it) }
                } catch (e: Exception) {
                    errorMessage = "Erreur Firebase : ${e.localizedMessage}"
                }
            }
        } catch (e: ApiException) {
            errorMessage = "Connexion Google annulée ou échouée."
        }
    }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLoginMode) "Connexion" else "Inscription",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TextButton(onClick = { showResetDialog = true }) {
                Text("Mot de passe oublié ?")
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email et mot de passe requis."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            try {
                                val result = if (isLoginMode) {
                                    auth.signInWithEmailAndPassword(email, password).await()
                                } else {
                                    auth.createUserWithEmailAndPassword(email, password).await()
                                }
                                onAuthComplete(result.user!!)
                            } catch (e: FirebaseAuthUserCollisionException) {
                                errorMessage = "Cet email est déjà utilisé."
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                errorMessage = "Mot de passe trop faible (6 caractères min)."
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Email ou mot de passe incorrect."
                            } catch (e: Exception) {
                                errorMessage = "Erreur: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoginMode) "Connexion" else "Créer un compte")
                }

                TextButton(onClick = { isLoginMode = !isLoginMode }) {
                    Text(
                        if (isLoginMode)
                            "Pas encore inscrit ? Créer un compte"
                        else
                            "Déjà un compte ? Se connecter"
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Button(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connexion avec Google")
                }
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showResetDialog = false
                        resetEmail = ""
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (resetEmail.isBlank()) return@TextButton
                            showResetDialog = false
                            val emailToSend = resetEmail.trim()
                            resetEmail = ""

                            coroutineScope.launch {
                                try {
                                    auth.sendPasswordResetEmail(emailToSend).await()
                                    snackbarHostState.showSnackbar("📧 Email de réinitialisation envoyé !")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("❌ ${e.localizedMessage}")
                                }
                            }
                        }) { Text("Envoyer") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showResetDialog = false
                            resetEmail = ""
                        }) { Text("Annuler") }
                    },
                    title = { Text("Réinitialiser le mot de passe") },
                    text = {
                        Column {
                            Text("Entrez votre adresse email pour recevoir un lien de réinitialisation.")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = resetEmail,
                                onValueChange = { resetEmail = it },
                                label = { Text("Email") },
                                singleLine = true
                            )
                        }
                    }
                )
            }
        }
    }
}
