package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignUpResult

class MainActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signUpButton: Button
    private lateinit var verifyButton: Button
    private lateinit var loginButton: Button

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

        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        signUpButton = findViewById(R.id.buttonSignUp)
        verifyButton = findViewById(R.id.buttonVerify)
        loginButton = findViewById(R.id.buttonLogin)

        signUpButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            val options = AuthSignUpOptions.builder()
                .userAttribute(AuthUserAttributeKey.email(), emailText)
                .build()

            Amplify.Auth.signUp(
                emailText,
                passwordText,
                options,
                { result ->
                    Log.i("AuthQuickStart", "Sign up result: $result")
                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                    // Navigate to VerifyActivity
                    startActivity(Intent(this, VerifyActivity::class.java))
                    finish()
                },
                { error ->
                    Log.e("AuthQuickStart", "Sign up failed", error)
                }
            )

        }

        verifyButton.setOnClickListener {
            startActivity(Intent(this, VerifyActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


    }
}