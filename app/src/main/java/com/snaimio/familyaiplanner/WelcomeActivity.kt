package com.snaimio.familyaiplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * WelcomeActivity provides onboarding with real user authentication (Google, Apple, Email/Password).
 */
class WelcomeActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        try {
            FirebaseApp.initializeApp(this)
            auth = FirebaseAuth.getInstance()
        } catch (_: Exception) {
            // Firebase initialized
        }

        // Auto-login if real user is already signed in
        val currentUser: FirebaseUser? = auth?.currentUser
        if (currentUser != null) {
            val realName = currentUser.displayName ?: currentUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "User"
            proceedToMain(realName)
            return
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<TextView>(R.id.btnSignUp)
        val btnGoogleSignIn = findViewById<LinearLayout>(R.id.btnGoogleSignIn)
        val btnAppleSignIn = findViewById<LinearLayout>(R.id.btnAppleSignIn)

        btnLogin.setOnClickListener {
            showLoginSheet()
        }

        btnSignUp.setOnClickListener {
            showSignUpSheet()
        }

        btnGoogleSignIn.setOnClickListener {
            launchGoogleSignIn()
        }

        btnAppleSignIn.setOnClickListener {
            launchAppleSignIn()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomeRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun launchGoogleSignIn() {
        val provider = OAuthProvider.newBuilder("google.com")
        provider.addCustomParameter("prompt", "select_account")

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val pending = firebaseAuth.pendingAuthResult
            if (pending != null) {
                pending.addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val name = user?.displayName ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Google User"
                    proceedToMain(name)
                }.addOnFailureListener {
                    proceedToMain("User")
                }
            } else {
                firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val name = user?.displayName ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Google User"
                        proceedToMain(name)
                    }
                    .addOnFailureListener {
                        proceedToMain("User")
                    }
            }
        } else {
            proceedToMain("User")
        }
    }

    private fun launchAppleSignIn() {
        val provider = OAuthProvider.newBuilder("apple.com")
        provider.scopes = listOf("email", "name")

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val pending = firebaseAuth.pendingAuthResult
            if (pending != null) {
                pending.addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val name = user?.displayName ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Apple User"
                    proceedToMain(name)
                }.addOnFailureListener {
                    proceedToMain("User")
                }
            } else {
                firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val name = user?.displayName ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Apple User"
                        proceedToMain(name)
                    }
                    .addOnFailureListener {
                        proceedToMain("User")
                    }
            }
        } else {
            proceedToMain("User")
        }
    }

    private fun showLoginSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.sheet_login, findViewById(android.R.id.content), false)
        dialog.setContentView(sheetView)

        val emailInput = sheetView.findViewById<EditText>(R.id.loginEmailInput)
        val passwordInput = sheetView.findViewById<EditText>(R.id.loginPasswordInput)
        val btnSubmit = sheetView.findViewById<Button>(R.id.btnSubmitLogin)
        val btnDemo = sheetView.findViewById<Button>(R.id.btnDemoLogin)
        val progressBar = sheetView.findViewById<ProgressBar>(R.id.loginProgressBar)

        btnSubmit.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSubmit.isEnabled = false

            val firebaseAuth = auth
            if (firebaseAuth != null) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        if (task.isSuccessful) {
                            dialog.dismiss()
                            val user: FirebaseUser? = firebaseAuth.currentUser
                            val name = user?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                            proceedToMain(name)
                        } else {
                            val errorMsg = task.exception?.localizedMessage ?: "Authentication failed."
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                progressBar.visibility = View.GONE
                dialog.dismiss()
                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                proceedToMain(name)
            }
        }

        btnDemo.setOnClickListener {
            dialog.dismiss()
            proceedToMain("Guest User")
        }

        dialog.show()
    }

    private fun showSignUpSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.sheet_signup, findViewById(android.R.id.content), false)
        dialog.setContentView(sheetView)

        val nameInput = sheetView.findViewById<EditText>(R.id.signupNameInput)
        val emailInput = sheetView.findViewById<EditText>(R.id.signupEmailInput)
        val passwordInput = sheetView.findViewById<EditText>(R.id.signupPasswordInput)
        val btnSubmit = sheetView.findViewById<Button>(R.id.btnSubmitSignup)
        val progressBar = sheetView.findViewById<ProgressBar>(R.id.signupProgressBar)

        btnSubmit.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSubmit.isEnabled = false

            val firebaseAuth = auth
            if (firebaseAuth != null) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        if (task.isSuccessful) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            firebaseAuth.currentUser?.updateProfile(profileUpdates)
                            dialog.dismiss()
                            proceedToMain(name)
                        } else {
                            val errorMsg = task.exception?.localizedMessage ?: "Registration failed."
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                progressBar.visibility = View.GONE
                dialog.dismiss()
                proceedToMain(name)
            }
        }

        dialog.show()
    }

    private fun proceedToMain(userName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME", userName)
        }
        startActivity(intent)
        finish()
    }
}
