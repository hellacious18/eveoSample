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

class VerifyActivity : AppCompatActivity() {

    private lateinit var verifyEmail: EditText
    private lateinit var verifyCode: EditText
    private lateinit var verifyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify)

        verifyEmail = findViewById(R.id.editTextVerifyEmail)
        verifyCode = findViewById(R.id.editTextVerifyCode)
        verifyButton = findViewById(R.id.buttonVerify)

        verifyButton.setOnClickListener {
            val email = verifyEmail.text.toString()
            val verificationCode = verifyCode.text.toString()

//            try {
//                val code = verificationCode
//                val result = Amplify.Auth.confirmSignUp("username", code)
//                if (result.isSignUpComplete) {
//                    Log.i("AuthQuickstart", "Signup confirmed")
//                } else {
//                    Log.i("AuthQuickstart", "Signup confirmation not yet complete")
//                }
//            } catch (error: AuthException) {
//                Log.e("AuthQuickstart", "Failed to confirm signup", error)
//            }
            Amplify.Auth.confirmSignUp(
                email, verificationCode,
                { result ->
                    if (result.isSignUpComplete) {
                        Log.i("AuthQuickstart", "Confirm signUp succeeded")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Log.i("AuthQuickstart","Confirm sign up not complete")
                    }
                },
                { Log.e("AuthQuickstart", "Failed to confirm sign up", it) }
            )
        }

    }
}