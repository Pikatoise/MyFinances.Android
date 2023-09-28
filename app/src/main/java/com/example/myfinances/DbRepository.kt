package com.example.myfinances

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import java.io.IOException
import java.text.DecimalFormat
import java.util.Locale


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
			mDbHelper.writableDatabase
		} catch (mSQLException: SQLException) {
			throw mSQLException
		}
	}

	public fun getCurrentPeriod(): Period{
		var period: Period = Period(-1,-1,-1,false)

		val cursor = mDb.rawQuery(
			"SELECT * FROM period WHERE isCurrent = '1'",
			null)

		cursor.moveToFirst()

		period.id = cursor.getInt(0)
		period.year = cursor.getInt(1)
		period.month = cursor.getInt(2)
		period.isCurrent = true

		cursor.close()

		return period
	}

	public fun getPeriodOperations(periodId: Int): ArrayList<Operation>{
		var operations: ArrayList<Operation> = arrayListOf()

		val cursor = mDb.rawQuery(
			"SELECT * FROM operation WHERE periodId = '${periodId}'",
			null)

		cursor.moveToFirst()

		while (!cursor.isAfterLast) {
			val id: Int = cursor.getInt(0)
			val type: Int = cursor.getInt(1)
			val title: String = cursor.getString(2)
			val amount: Double = cursor.getDouble(3)
			val periodId: Int = periodId

			operations.add(Operation(id,type,title,amount,periodId))

			cursor.moveToNext()
		}

		cursor.close()

		return operations
	}

	public fun getMonthlyExpensesInRub(): Double{
		val currentPeriod: Period = getCurrentPeriod()
		val operations = getPeriodOperations(currentPeriod.id)
		var expenses: Double = 0.0

		operations.forEach{
			expenses += it.amount
		}

		return expenses
	}
}