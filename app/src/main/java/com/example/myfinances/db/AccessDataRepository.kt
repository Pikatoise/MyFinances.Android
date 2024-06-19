package com.example.myfinances.db

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.IOException
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.Date

class AccessDataRepository(preferencesContext: SharedPreferences){
    companion object {
        public val preferencesName = "accessData"
    }

    private val preferences = preferencesContext
    
    private val USER_ID_KEY = "userId"
    private val ACCESS_TOKEN_KEY = "accessToken"
    private val REFRESH_TOKEN_KEY = "refreshToken"
    private val LAST_REFRESH_KEY = "lastRefresh"

    public fun getUserId(): Int {
        return preferences.getInt(USER_ID_KEY, -1)
    }

    public fun getAccessToken(): String? {
        return preferences.getString(ACCESS_TOKEN_KEY,  "")
    }

    public fun getRefreshToken(): String? {
        return preferences.getString(REFRESH_TOKEN_KEY, "")
    }

    public fun getLastRefresh(): String? {
        return preferences.getString(LAST_REFRESH_KEY, "")
    }

    public fun updateUserId(id: Int) {
        preferences.edit().apply{
            putInt(USER_ID_KEY, id)

            apply()
        }
    }

    public fun updateAccessToken(token: String) {
        preferences.edit().apply{
            putString(ACCESS_TOKEN_KEY, token)

            apply()
        }
    }

    public fun updateRefreshToken(token: String) {
        preferences.edit().apply{
            putString(REFRESH_TOKEN_KEY, token)

            apply()
        }
    }

    public fun updateLastRefresh(lastRefresh: String) {
        preferences.edit().apply{
            putString(LAST_REFRESH_KEY, lastRefresh)

            apply()
        }
    }
}