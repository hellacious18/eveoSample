package com.example.eveosample

import android.content.Intent
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
import android.widget.TextView
import android.widget.Toast
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignUpResult

class MainActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: TextView
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        signUpButton = findViewById(R.id.buttonSignUp)
        loginButton = findViewById(R.id.textViewLogIn)
        forgotPassword = findViewById(R.id.textViewForgotPassword)

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

    }
}