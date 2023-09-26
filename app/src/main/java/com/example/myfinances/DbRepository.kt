package com.example.myfinances

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import java.io.IOException


class DbRepository(context: Context) {
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
			mDbHelper.getWritableDatabase()
		} catch (mSQLException: SQLException) {
			throw mSQLException
		}
	}

	public fun GetCurrentPeriod(): String{
		var period = ""

		val cursor = mDb.rawQuery("SELECT * FROM period WHERE isCurrent = 1", null)
		cursor.moveToFirst()
		while (!cursor.isAfterLast) {
			period += "${cursor.getString(0)} ${cursor.getString(1)} ${cursor.getString(2)} ${cursor.getString(3)}"
			cursor.moveToNext()
		}
		cursor.close()

		return period
	}
}