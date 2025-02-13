package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.android.gms.auth.api.signin.GoogleSignIn

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        AWSMobileClient.getInstance().initialize(this, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails) {
                Log.d("AWSMobileClient", "Initialized: ${result.userState}")
            }
            override fun onError(e: Exception) {
                Log.e("AWSMobileClient", "Initialization error", e)
            }
        })

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Successfully initialized Amplify")
        } catch (e: Exception) {
            Log.e("Amplify", "Amplify initialization failed", e)
        }

        checkIfUserIsSignedIn()

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkIfUserIsSignedIn() {
        // Check if user is logged in via Amplify (AWS Cognito)
        Amplify.Auth.fetchAuthSession(
            { authSession ->
                if (authSession.isSignedIn) {
                    navigateToHome()
                }
            },
            { _: AuthException -> }
        )

        // Check if user is logged in via Google
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
