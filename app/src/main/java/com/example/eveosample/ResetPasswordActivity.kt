package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.AuthException
import com.amplifyframework.core.Amplify

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var email: TextView
    private lateinit var password: EditText
    private lateinit var password2: EditText
    private lateinit var resetPasswordButton: Button

    private lateinit var otpBox1: EditText
    private lateinit var otpBox2: EditText
    private lateinit var otpBox3: EditText
    private lateinit var otpBox4: EditText
    private lateinit var otpBox5: EditText
    private lateinit var otpBox6: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        email = findViewById(R.id.textViewEmail)
        password = findViewById(R.id.editTextPassword)
        password2 = findViewById(R.id.editTextPassword2)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        otpBox1 = findViewById(R.id.opt1)
        otpBox2 = findViewById(R.id.opt2)
        otpBox3 = findViewById(R.id.opt3)
        otpBox4 = findViewById(R.id.opt4)
        otpBox5 = findViewById(R.id.opt5)
        otpBox6 = findViewById(R.id.opt6)

        val eMail = intent.getStringExtra("EMAIL_KEY") ?: ""
        email.text = eMail

        // Set up OTP auto-focus
        otpBox1.addTextChangedListener(GenericTextWatcher(otpBox1, nextView = otpBox2))
        otpBox2.addTextChangedListener(GenericTextWatcher(otpBox2, nextView = otpBox3, prevView = otpBox1))
        otpBox3.addTextChangedListener(GenericTextWatcher(otpBox3, nextView = otpBox4, prevView = otpBox2))
        otpBox4.addTextChangedListener(GenericTextWatcher(otpBox4, nextView = otpBox5, prevView = otpBox3))
        otpBox5.addTextChangedListener(GenericTextWatcher(otpBox5, nextView = otpBox6, prevView = otpBox4))
        otpBox6.addTextChangedListener(GenericTextWatcher(otpBox6, prevView = otpBox5))

        resetPasswordButton.setOnClickListener {
            val emailStr = eMail.trim()
            val newPassword = password.text.toString()
            val confirmPassword = password2.text.toString()

            // Concatenate OTP from all boxes
            val otpCode = otpBox1.text.toString() +
                    otpBox2.text.toString() +
                    otpBox3.text.toString() +
                    otpBox4.text.toString() +
                    otpBox5.text.toString() +
                    otpBox6.text.toString()

            Log.i("OTP", "OTP Code: $otpCode")

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (otpCode.length != 6) {
                Toast.makeText(this, "Please enter the complete OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Confirm the reset password using Amplify
            Amplify.Auth.confirmResetPassword(
                emailStr,
                newPassword,
                otpCode,
                {
                    Log.i("AuthQuickstart", "New password confirmed")
                    runOnUiThread {
                        Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show()
                    }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                },
                { error: AuthException ->
                    Log.e("AuthQuickstart", "Failed to confirm password reset", error)
                    runOnUiThread {
                        Toast.makeText(this, "Failed to confirm password reset", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

class GenericTextWatcher(
    private val currentView: EditText,
    private val nextView: EditText? = null,
    private val prevView: EditText? = null
) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        when {
            s?.length == 1 -> nextView?.requestFocus()
            s?.isEmpty() == true -> prevView?.requestFocus()
        }
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
}
