package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amplifyframework.core.Amplify

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var sendCodeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        email = findViewById(R.id.editTextEmail)
        sendCodeButton = findViewById(R.id.buttonSendCode)

        sendCodeButton.setOnClickListener {
            val email = email.text.toString()

            Amplify.Auth.resetPassword(email,
                { Log.i("AuthQuickstart", "Password reset OK: $it")
                    startActivity(Intent(this, ResetPasswordActivity::class.java))
                    finish()
                },
                { Log.e("AuthQuickstart", "Password reset failed", it) }
            )
        }

    }
}