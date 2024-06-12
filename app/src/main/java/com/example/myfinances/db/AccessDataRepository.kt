package com.example.myfinances.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.IOException
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.Date

class AccessDataRepository(context: Context){
    private var mDbHelper: DatabaseHelper
    private var mDb: SQLiteDatabase

    init{
        mDbHelper = DatabaseHelper(context)

        try {
            mDbHelper.updateDataBase()
        } catch (mIOException: IOException) {
            throw Error("UnableToUpdateDatabase")
        }

        mDb = try {
            mDbHelper.writableDatabase
        } catch (mSQLException: SQLException) {
            throw mSQLException
        }
    }

    public fun getUserId(): Int {
        var userId: Int = -1

        var cursor = mDb.rawQuery(
            "SELECT userId FROM AccessData",
            null
        )

        if (cursor != null && cursor.count > 0){
            cursor.moveToFirst()

            userId = cursor.getInt(0)
        }

        cursor.close()

        return userId
    }

    public fun getAccessToken(): String? {
        var accessToken: String? = null

        var cursor = mDb.rawQuery(
            "SELECT accessToken FROM AccessData",
            null
        )

        if (cursor != null && cursor.count > 0){
            cursor.moveToFirst()

            accessToken = cursor.getString(1)
        }

        cursor.close()

        return accessToken
    }

    public fun getRefreshToken(): String? {
        var refreshToken: String? = null

        var cursor = mDb.rawQuery(
            "SELECT refreshToken FROM AccessData",
            null
        )

        if (cursor != null && cursor.count > 0){
            cursor.moveToFirst()

            refreshToken = cursor.getString(2)
        }

        cursor.close()

        return refreshToken
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun getLastRefresh(): LocalDateTime? {
        var lastRefresh: String? = null

        var cursor = mDb.rawQuery(
            "SELECT lastRefresh FROM AccessData",
            null
        )

        if (cursor != null && cursor.count > 0){
            cursor.moveToFirst()

            lastRefresh = cursor.getString(2)
        }

        cursor.close()

        return LocalDateTime.parse(lastRefresh)
    }

    public fun updateUserId(id: Int) {
        mDb.execSQL("UPDATE AccessData SET userId = ${id}")
    }

    public fun updateAccessToken(token: Int) {
        mDb.execSQL("UPDATE AccessData SET accessToken = ${token}")
    }

    public fun updateRefreshToken(token: Int) {
        mDb.execSQL("UPDATE AccessData SET refreshToken = ${token}")
    }

    public fun updateLastRefreshToken(lastRefresh: LocalDateTime) {
        val lastRefreshStr = lastRefresh.toString()
        mDb.execSQL("UPDATE AccessData SET refreshToken = ${lastRefreshStr}")
    }
}