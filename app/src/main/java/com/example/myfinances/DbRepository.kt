package com.example.myfinances

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import java.io.IOException
import java.io.Serializable
import java.util.Calendar


class DbRepository(context: Context){
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

	public fun updateCurrentPeriod() {
		val currentYear = Calendar.getInstance().get(Calendar.YEAR)
		val currentMonth = Calendar.getInstance().get(Calendar.MONTH+1)

		var period: Period = Period(-1,-1,-1,false)

		var cursor = mDb.rawQuery(
			"SELECT * FROM period WHERE isCurrent = '1'",
			null)

		if (cursor != null && cursor.count > 0){
			cursor.moveToFirst()

			period.id = cursor.getInt(0)
			period.year = cursor.getInt(1)
			period.month = cursor.getInt(2)
		}

		cursor.close()

		if (period.month == currentMonth && period.year == currentYear)
			return

		mDb.execSQL("UPDATE period SET isCurrent = '0' WHERE isCurrent = '1'")

		mDb.execSQL("INSERT INTO period(year,month,isCurrent) VALUES (${currentYear},${currentMonth},'1')")
	}

	public fun getCurrentPeriod(): Period{
		var period: Period = Period(-1,-1,-1,false)

		var cursor = mDb.rawQuery(
			"SELECT * FROM period WHERE isCurrent = '1'",
			null)

		if (cursor != null && cursor.count > 0){
			cursor.moveToFirst()

			period.id = cursor.getInt(0)
			period.year = cursor.getInt(1)
			period.month = cursor.getInt(2)
			period.isCurrent = true
		}

		cursor.close()

		return period
	}

	public fun getPeriodOperations(periodId: Int): ArrayList<Operation>{
		var operations: ArrayList<Operation> = arrayListOf()

		val cursor = mDb.rawQuery(
			"SELECT * FROM operation WHERE periodId = '${periodId}'",
			null)

		if (cursor != null && cursor.count > 0) {
			cursor.moveToFirst()

			while (!cursor.isAfterLast) {
				val id: Int = cursor.getInt(0)
				val type: Int = cursor.getInt(1)
				val title: String = cursor.getString(2)
				val amount: Double = cursor.getDouble(3)

				operations.add(Operation(id,type,title,amount,periodId))

				cursor.moveToNext()
			}
		}

		cursor.close()

		return operations
	}

	public fun getMonthlyExpensesInRub(period: Period): Double{
		val operations = getPeriodOperations(period.id)
		var expenses: Double = 0.0

		operations.forEach{
			expenses += it.amount
		}

		return expenses
	}

	public fun addOperation(type: Int?, title: String?, amount: Double?){
		val period = getCurrentPeriod()

		mDb.execSQL(
			"INSERT INTO operation(type,title,amount,periodId) VALUES ('${type}','${title}','${amount}','${period.id}')")
	}

	public fun addOperation(periodId: Int, type: Int?, title: String?, amount: Double?){
		mDb.execSQL(
			"INSERT INTO operation(type,title,amount,periodId) VALUES ('${type}','${title}','${amount}','${periodId}')")
	}

	public fun removeOperation(id: Int){
		mDb.execSQL(
			"DELETE FROM operation WHERE id=${id}")
	}

	public fun DbDebugMode() {
		addOperation(9,"Зарплата",34000.0)
		addOperation(1,"Продукты",-400.0)
		addOperation(4,"Одежда",-2300.0)
		addOperation(3,"Проценты вклада",300.0)
		addOperation(11,"Бургер кинг",-260.0)
		addOperation(8,"Лекарства",-600.0)
		addOperation(0,"Алкоголь",-350.0)
		addOperation(11,"Бургер кинг",-260.0)
		addOperation(1,"Продукты",-459.0)
		addOperation(9,"Зарплата",12000.0)

		val currentYear = Calendar.getInstance().get(Calendar.YEAR)
		val currentMonth = Calendar.getInstance().get(Calendar.MONTH+1)

		mDb.execSQL("INSERT INTO period(year,month) VALUES(${currentYear},${currentMonth-1})")
		mDb.execSQL("INSERT INTO period(year,month) VALUES(${currentYear},${currentMonth-2})")

		var firstMonthId = -1
		var secondMonthId = -1

		var cursor = mDb.rawQuery(
			"SELECT * FROM period WHERE month = '${currentMonth-1}'",
			null)

		if (cursor != null && cursor.count > 0){
			cursor.moveToFirst()

			firstMonthId = cursor.getInt(0)
		}

		cursor.close()

		cursor = mDb.rawQuery(
			"SELECT * FROM period WHERE month = '${currentMonth-2}'",
			null)

		if (cursor != null && cursor.count > 0){
			cursor.moveToFirst()

			secondMonthId = cursor.getInt(0)
		}

		cursor.close()

		if (firstMonthId != -1){
			addOperation(firstMonthId,4,"Одежда",-9600.0)
			addOperation(firstMonthId,1,"Продукты",-400.0)
			addOperation(firstMonthId,2,"Такси",-250.0)
		}

		if (secondMonthId != -1){
			addOperation(secondMonthId,9,"Зарплата",24000.0)
			addOperation(secondMonthId,11,"KFC",-360.0)
			addOperation(secondMonthId,1,"Продукты",-700.0)
			addOperation(secondMonthId,8,"Лекарства",-300.0)
		}
	}

	public fun getPeriodsCount(): Int{
		var periodsId: ArrayList<Int> = arrayListOf()

		val cursor = mDb.rawQuery(
			"SELECT * FROM period",
			null)

		if (cursor != null && cursor.count > 0) {
			cursor.moveToFirst()

			while (!cursor.isAfterLast) {
				val id: Int = cursor.getInt(0)

				periodsId.add(id)

				cursor.moveToNext()
			}
		}

		cursor.close()

		return periodsId.size
	}

	public fun getOperationsCount(): Int{
		var operationsId: ArrayList<Int> = arrayListOf()

		val cursor = mDb.rawQuery(
			"SELECT * FROM operation",
			null)

		if (cursor != null && cursor.count > 0) {
			cursor.moveToFirst()

			while (!cursor.isAfterLast) {
				val id: Int = cursor.getInt(0)

				operationsId.add(id)

				cursor.moveToNext()
			}
		}

		cursor.close()

		return operationsId.size
	}
}