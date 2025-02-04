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
import com.amplifyframework.auth.AuthException
import com.amplifyframework.core.Amplify

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var code: EditText
    private lateinit var password: EditText
    private lateinit var password2: EditText
    private lateinit var resetPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        email = findViewById(R.id.editTextEmail)
        code = findViewById(R.id.editTextCode)
        password = findViewById(R.id.editTextPassword)
        password2 = findViewById(R.id.editTextPassword2)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        resetPasswordButton.setOnClickListener {
            val email = email.text.toString()
            val code = code.text.toString()
            val password = password.text.toString()
            val password2 = password2.text.toString()

            if(password != password2){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Amplify.Auth.confirmResetPassword(email, password, code,
                { Log.i("AuthQuickstart", "New password confirmed")
//                    Toast.makeText(this, "New Password Updated", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                },
                { Log.e("AuthQuickstart", "Failed to confirm password reset", it) }
            )
        }
    }
}