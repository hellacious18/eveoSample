package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amplifyframework.core.Amplify

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var backbutton: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        forgotPassword = findViewById(R.id.textViewForgotPassword)
        backbutton = findViewById(R.id.backbutton)

        backbutton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        loginButton.setOnClickListener {

            val email = email.text.toString()
            val password = password.text.toString()

            Amplify.Auth.signIn(email, password,
                { result ->
                    if (result.isSignedIn) {

                        //shared pref
                        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                        sharedPref.edit().putString("EMAIL_KEY", email).apply()

                        Log.d("Login", "Log in succeeded")
                        runOnUiThread { Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show() }
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("EMAIL_KEY", email)  // Pass the email
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("Login", "Log in not complete")
                    }
                },
                { Log.d("Login", "Failed to Log in", it) }
            )
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