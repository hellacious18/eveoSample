package com.example.eveosample

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.Html
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignUpResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "MainActivity"
    }

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: TextView
    private lateinit var forgotPassword: TextView
    private lateinit var googleSignInButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        signUpButton = findViewById(R.id.buttonSignUp)
        loginButton = findViewById(R.id.textViewLogIn)
        forgotPassword = findViewById(R.id.textViewForgotPassword)
        googleSignInButton = findViewById(R.id.imageViewGoogle)

        // Initialize Amplify
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized Amplify")
        } catch (e: Exception) {
            Log.e("Amplify", "Initialization failed", e)
        }

        // If signed in, navigate to HomeActivity
        Amplify.Auth.fetchAuthSession(
            { authSession ->
                if (authSession.isSignedIn) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()  // Close MainActivity so the user can't go back
                }
            },
            { error: AuthException ->
//                Toast.makeText(this, "Error checking sign-in status: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Request user's email address.
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check if the user is already signed in. If so, navigate to HomeActivity.
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                putExtra("EMAIL_KEY", account.email)
            })
            finish()
            return
        }

        signUpButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val options = AuthSignUpOptions.builder()
                .userAttribute(AuthUserAttributeKey.email(), emailText)
                .build()

            Amplify.Auth.signUp(
                emailText,
                passwordText,
                options,
                { result ->
                    Log.d("AuthQuickStart", "Sign up result: $result")
                    val intent = Intent(this, VerifyActivity::class.java)
                    intent.putExtra("EMAIL_KEY", emailText)  // Pass the email
                    intent.putExtra("IS_NEW_USER", true)
                    startActivity(intent)

                },
                { error ->
                    Log.d("AuthQuickStart", "Sign up failed", error)
                }
            )

        }

        val htmlText = "Already have an account? <u><b>Log In</b></u>"
        loginButton.text = Html.fromHtml(htmlText)
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        forgotPassword.paintFlags = forgotPassword.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        forgotPassword.setOnClickListener {
            val email = email.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Amplify.Auth.resetPassword(email,
                { result ->
                    Log.i("AuthQuickstart", "Password reset OK: $result")
                    runOnUiThread {
                        Toast.makeText(this, "Check your email for reset code", Toast.LENGTH_SHORT).show()
                    }
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("EMAIL_KEY", email)  // Pass the email
                    startActivity(intent)
                    finish()
                },
                { error ->
                    Log.e("AuthQuickstart", "Password reset failed: ${error.localizedMessage}", error)
                    runOnUiThread {
                        Toast.makeText(this, "Password reset failed: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            )

        }



        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Successfully signed in.
                Log.d(TAG, "Google sign-in successful. Email: ${account?.email}")
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("EMAIL_KEY", account?.email)  // Pass the email
                startActivity(intent)
                finish()
            } catch (e: ApiException) {
                // Sign in failed, display a message to the user.
                Log.w(TAG, "Google sign-in failed, code=" + e.statusCode)
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
