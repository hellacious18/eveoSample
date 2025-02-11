package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: TextView
    private lateinit var forgotPassword: TextView
    private lateinit var googleSignInButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        signUpButton = findViewById(R.id.buttonSignUp)
        loginButton = findViewById(R.id.textViewLogIn)
        forgotPassword = findViewById(R.id.textViewForgotPassword)
        googleSignInButton = findViewById(R.id.imageViewGoogle)

        setupGoogleSignIn()
        handleAuthChecks()
        setupListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.GOOGLE_WEBCLIENT_ID))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun handleAuthChecks() {
        // Redirect signed-in user to HomeActivity
        Amplify.Auth.fetchAuthSession(
            { authSession ->
                if (authSession.isSignedIn) {
                    navigateToHome()
                }
            },
            { error: AuthException -> Log.e("AuthCheck", "Error checking sign-in status", error) }
        )

        val account = GoogleSignIn.getLastSignedInAccount(this)
        account?.let {
            saveEmailToPrefs(it.email)
            navigateToHome()
        }
    }

    private fun setupListeners() {
        signUpButton.setOnClickListener { handleSignUp() }

        loginButton.text = Html.fromHtml("Already have an account? <u><b>Log In</b></u>")
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        forgotPassword.paintFlags = forgotPassword.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        forgotPassword.setOnClickListener { handlePasswordReset() }

        googleSignInButton.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }
    }

    private fun handleSignUp() {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), emailText)
            .build()

        Amplify.Auth.signUp(emailText, passwordText, options,
            {
                val intent = Intent(this, VerifyActivity::class.java)
                intent.putExtra("EMAIL_KEY", emailText)
                intent.putExtra("IS_NEW_USER", true)
                startActivity(intent)
            },
            { error -> Log.e("SignUp", "Sign-up failed", error) }
        )
    }

    private fun handlePasswordReset() {
        val emailText = email.text.toString()
        if (emailText.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }

        Amplify.Auth.resetPassword(emailText,
            {
                runOnUiThread {
                    Toast.makeText(this, "Check your email for reset code", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this, ResetPasswordActivity::class.java).apply {
                    putExtra("EMAIL_KEY", emailText)
                })
                finish()
            },
            { error -> Log.e("PasswordReset", "Failed: ${error.localizedMessage}", error) }
        )
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun saveEmailToPrefs(email: String?) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE)
            .edit()
            .putString("EMAIL_KEY", email)
            .apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("GoogleSignIn", "Success. Email: ${account?.email}, ID Token: ${account?.idToken}")
                saveEmailToPrefs(account?.email)
                navigateToHome()
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Failed: ${e.statusCode}")
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
