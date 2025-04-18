package com.example.makeitmeme

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// Classe très simple pour tenir l'instance Auth
// Dans une vraie app, ce serait un ViewModel Android complet
object AuthManager {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}

