package com.cl.common_base.util.login

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.cl.common_base.ext.logE
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * 谷歌登录
 */
class GoogleLoginHelper(val activity: Activity) {
    val oneTapClient by lazy {
        Identity.getSignInClient(activity)
    }

    val auth by lazy {
        Firebase.auth
    }

    private val signUpRequest by lazy {
        BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true) // Your server's client ID, not your Android client ID.
                    .setServerClientId("995131742482-i3vj9v7h9nh8hfbu36jtaju9ul4jgdoi.apps.googleusercontent.com") // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false).build()
            ).build()
    }

    private val signInRequest by lazy {
        BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true) // Your server's client ID, not your Android client ID.
                    .setServerClientId("995131742482-i3vj9v7h9nh8hfbu36jtaju9ul4jgdoi.apps.googleusercontent.com") // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(true).build()
            ).build()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun login() {
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(activity) { result ->
                kotlin.runCatching {
                    activity.startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP, null, 0, 0, 0, null
                    )
                }.onFailure {
                    logE("signInRequest Couldn't start One Tap UI")
                }
            }.addOnFailureListener(activity) { e -> // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                logE("signInRequest: ${e.localizedMessage}")
                oneTapClient.beginSignIn(signUpRequest).addOnSuccessListener(activity) { result ->
                        kotlin.runCatching {
                            activity.startIntentSenderForResult(
                                result.pendingIntent.intentSender, REQ_ONE_TAP, null, 0, 0, 0
                            )
                        }.onFailure {
                            logE("signUpRequest Couldn't start One Tap UI")
                        }
                    }.addOnFailureListener(activity) { e -> // No Google Accounts found. Just continue presenting the signed-out UI.
                        logE("signUpRequest ${e.localizedMessage}")
                    }
            }
    }

    companion object {
        const val REQ_ONE_TAP = 2
    }
}