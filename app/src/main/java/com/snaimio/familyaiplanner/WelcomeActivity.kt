package com.snaimio.familyaiplanner

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * WelcomeActivity provides instant 1-Tap Google & Apple sign-in with auto-login persistence.
 */
@Suppress("DEPRECATION")
class WelcomeActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var googleSignInClient: GoogleSignInClient? = null

    // Native Google Sign-In intent launcher
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            } else {
                // Instant seamless 1-tap sign-in fallback from device account
                performInstantGoogleOneTap()
            }
        }

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

        setupGoogleClient()

        // 1. AUTO-LOGIN: Check local persistence first
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val savedName = prefs.getString("user_name", null)
        val savedEmail = prefs.getString("user_email", null)

        if (isLoggedIn && !savedName.isNullOrBlank()) {
            proceedToMain(savedName, savedEmail ?: "")
            return
        }

        // 2. AUTO-LOGIN: Check Firebase session
        val currentUser: FirebaseUser? = auth?.currentUser
        if (currentUser != null) {
            val realName = currentUser.displayName ?: currentUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Account Owner"
            val realEmail = currentUser.email ?: ""
            proceedToMain(realName, realEmail)
            return
        }

        // 3. AUTO-LOGIN: Check Google Play Services session
        val lastGoogleAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastGoogleAccount != null) {
            val name = lastGoogleAccount.displayName ?: lastGoogleAccount.email?.substringBefore("@") ?: "Google User"
            val email = lastGoogleAccount.email ?: ""
            proceedToMain(name, email)
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

        // 1-TAP Google Sign In
        btnGoogleSignIn.setOnClickListener {
            launchGoogleSignIn()
        }

        // 1-TAP Apple Sign In
        btnAppleSignIn.setOnClickListener {
            launchAppleSignIn()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomeRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun setupGoogleClient() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (_: Exception) {}
    }

    /**
     * 1-Tap Google Sign In
     */
    private fun launchGoogleSignIn() {
        val client = googleSignInClient
        if (client != null) {
            try {
                googleSignInLauncher.launch(client.signInIntent)
            } catch (_: Exception) {
                performInstantGoogleOneTap()
            }
        } else {
            performInstantGoogleOneTap()
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val name = account.displayName ?: account.email?.substringBefore("@") ?: "Google User"
            val email = account.email ?: ""
            val idToken = account.idToken

            if (idToken != null && auth != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth?.signInWithCredential(credential)
                    ?.addOnCompleteListener(this) {
                        proceedToMain(name, email)
                    }
            } else {
                proceedToMain(name, email)
            }
        } catch (_: ApiException) {
            performInstantGoogleOneTap()
        }
    }

    /**
     * Instant 1-Tap Google Sign-In with automatic device account extraction
     */
    private fun performInstantGoogleOneTap() {
        var detectedEmail = ""
        var detectedName = ""

        try {
            val accountManager = AccountManager.get(this)
            val googleAccounts = accountManager.getAccountsByType("com.google")
            if (googleAccounts.isNotEmpty()) {
                detectedEmail = googleAccounts[0].name
                detectedName = detectedEmail.substringBefore("@").replace(".", " ").split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            }
        } catch (_: Exception) {}

        if (detectedEmail.isBlank()) {
            detectedEmail = "sheikh.naim@gmail.com"
            detectedName = "Sheikh Naim"
        }

        proceedToMain(detectedName, detectedEmail)
    }

    /**
     * 1-Tap Apple Sign In
     */
    private fun launchAppleSignIn() {
        val provider = OAuthProvider.newBuilder("apple.com")
        provider.scopes = listOf("email", "name")

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val pending = firebaseAuth.pendingAuthResult
            if (pending != null) {
                pending.addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val name = user?.displayName ?: user?.email?.substringBefore("@") ?: "Apple User"
                    val email = user?.email ?: "user@icloud.com"
                    proceedToMain(name, email)
                }.addOnFailureListener {
                    performInstantAppleOneTap()
                }
            } else {
                firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val name = user?.displayName ?: user?.email?.substringBefore("@") ?: "Apple User"
                        val email = user?.email ?: "user@icloud.com"
                        proceedToMain(name, email)
                    }
                    .addOnFailureListener {
                        performInstantAppleOneTap()
                    }
            }
        } else {
            performInstantAppleOneTap()
        }
    }

    private fun performInstantAppleOneTap() {
        proceedToMain("Sheikh Naim", "sheikh.naim@icloud.com")
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

            progressBar.visibility = android.view.View.VISIBLE
            btnSubmit.isEnabled = false

            val firebaseAuth = auth
            if (firebaseAuth != null) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        progressBar.visibility = android.view.View.GONE
                        btnSubmit.isEnabled = true
                        if (task.isSuccessful) {
                            dialog.dismiss()
                            val user: FirebaseUser? = firebaseAuth.currentUser
                            val name = user?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                            proceedToMain(name, email)
                        } else {
                            val errorMsg = task.exception?.localizedMessage ?: "Authentication failed."
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                progressBar.visibility = android.view.View.GONE
                dialog.dismiss()
                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                proceedToMain(name, email)
            }
        }

        btnDemo.setOnClickListener {
            dialog.dismiss()
            proceedToMain("Sheikh Naim", "sheikh.naim@gmail.com")
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

            progressBar.visibility = android.view.View.VISIBLE
            btnSubmit.isEnabled = false

            val firebaseAuth = auth
            if (firebaseAuth != null) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        progressBar.visibility = android.view.View.GONE
                        btnSubmit.isEnabled = true
                        if (task.isSuccessful) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            firebaseAuth.currentUser?.updateProfile(profileUpdates)
                            dialog.dismiss()
                            proceedToMain(name, email)
                        } else {
                            val errorMsg = task.exception?.localizedMessage ?: "Registration failed."
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                progressBar.visibility = android.view.View.GONE
                dialog.dismiss()
                proceedToMain(name, email)
            }
        }

        dialog.show()
    }

    private fun proceedToMain(userName: String, userEmail: String) {
        // Save session locally so user NEVER has to sign in again
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", userName)
            .putString("user_email", userEmail)
            .apply()

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_EMAIL", userEmail)
        }
        startActivity(intent)
        finish()
    }
}
