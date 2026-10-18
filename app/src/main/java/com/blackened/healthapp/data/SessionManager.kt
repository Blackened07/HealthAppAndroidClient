package com.blackened.healthapp.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "health_app_prefs"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    var token: String?
        get() = sharedPreferences?.getString(KEY_JWT_TOKEN, null)
        set(value) {
            sharedPreferences?.edit()?.putString(KEY_JWT_TOKEN, value)?.apply()
            TokenManager.token = value
        }

    var userId: Long
        get() = sharedPreferences?.getLong(KEY_USER_ID, -1L) ?: -1L
        set(value) {
            sharedPreferences?.edit()?.putLong(KEY_USER_ID, value)?.apply()
        }

    var userName: String?
        get() = sharedPreferences?.getString(KEY_USER_NAME, null)
        set(value) {
            sharedPreferences?.edit()?.putString(KEY_USER_NAME, value)?.apply()
        }

    var userEmail: String?
        get() = sharedPreferences?.getString(KEY_USER_EMAIL, null)
        set(value) {
            sharedPreferences?.edit()?.putString(KEY_USER_EMAIL, value)?.apply()
        }

    fun isUserLoggedIn(): Boolean {
        return !token.isNullOrBlank()
    }

    fun clearSession() {
        sharedPreferences?.edit()?.clear()?.apply()
        TokenManager.token = null
    }
}