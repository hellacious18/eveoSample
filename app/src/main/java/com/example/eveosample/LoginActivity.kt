package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amplifyframework.core.Amplify

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin2)

        loginButton.setOnClickListener {

            val email = email.text.toString()
            val password = password.text.toString()

            Amplify.Auth.signIn(email, password,
                { result ->
                    if (result.isSignedIn) {
                        Log.d("Login", "Log in succeeded")
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Log.d("Login", "Log in not complete")
                    }
                },
                { Log.d("Login", "Failed to Log in", it) }
            )
        }
    }
}