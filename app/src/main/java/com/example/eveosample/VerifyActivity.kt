package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.core.Amplify

class VerifyActivity : AppCompatActivity() {

    private lateinit var verifyEmail: TextView
    private lateinit var verifyCode: EditText
    private lateinit var verifyButton: Button
    private lateinit var resendCode: TextView
    private lateinit var timer: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify)

        if (intent.getBooleanExtra("IS_NEW_USER", false)) {
            Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
        }

        verifyEmail = findViewById(R.id.editTextVerifyEmail)
        verifyCode = findViewById(R.id.editTextVerifyCode)
        resendCode = findViewById(R.id.textViewVerifyResendCode)
        verifyButton = findViewById(R.id.buttonVerify)
        timer = findViewById(R.id.textViewVerifyTimer)

        val email = intent.getStringExtra("EMAIL_KEY") ?: ""
        verifyEmail.setText(email) // Set the email in the EditText

        resendCode.paintFlags = resendCode.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        resendCode.setOnClickListener {
            val email = verifyEmail.text.toString()

            Amplify.Auth.resendSignUpCode(email,
                { Log.i("AuthQuickstart", "Resend was successful") },
                { Log.e("AuthQuickstart", "Resend failed", it) }
            )
            ResendTimer()
        }

        verifyButton.setOnClickListener {
            val email = verifyEmail.text.toString()
            val verificationCode = verifyCode.text.toString()

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

    private fun ResendTimer() {
        resendCode.isEnabled = false  // Disable button
        timer.visibility = View.VISIBLE  // Show timer text

        object : CountDownTimer(60000, 1000) {  // 60 seconds countdown
            override fun onTick(millisUntilFinished: Long) {
                timer.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                resendCode.isEnabled = true  // Enable button
                timer.visibility = View.GONE  // Hide timer
            }
        }.start()
    }
}