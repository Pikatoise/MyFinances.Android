package com.example.myfinances

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import java.io.IOException
import android.os.Build
import androidx.annotation.RequiresApi

class PlanRepository(context: Context) {
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

	@RequiresApi(Build.VERSION_CODES.O)
	public fun getFirstPlan(): Plan {
		var plan: Plan = Plan(-1,"","",-1,false)

		var cursor = mDb.rawQuery(
			"SELECT * FROM [plan]",
			null)

		if (cursor != null && cursor.count > 0){
			cursor.moveToFirst()

			plan.id = cursor.getInt(0)
			plan.name = cursor.getString(1)
			plan.date = cursor.getString(2)
			plan.type = cursor.getInt(3)
			plan.status = cursor.getInt(4) == 1
		}

		cursor.close()

		return plan
	}

	public fun getAllPlans(): ArrayList<Plan>{
		var plans : ArrayList<Plan> = arrayListOf()

		var cursor = mDb.rawQuery(
			"SELECT * FROM [plan]",
			null)

		if (cursor != null && cursor.count > 0){
			cursor.moveToFirst()

			while (!cursor.isAfterLast) {
				var id = cursor.getInt(0)
				var name = cursor.getString(1)
				var date = cursor.getString(2)
				var status = cursor.getInt(3) == 1
				var type = cursor.getInt(4)

				plans.add(Plan(id,name,date,type,status))

				cursor.moveToNext()
			}
		}
		cursor.close()

		return plans;
	}

	public fun changePlanStatus(planId: Int, newStatus: Boolean) {
		val newStatusInt: Int = if (newStatus) 1 else 0

		mDb.execSQL("UPDATE plan SET Status = $newStatusInt WHERE Id = $planId")
	}

	public fun removePlan(id: Int){
		mDb.execSQL(
			"DELETE FROM plan WHERE Id=${id}")
	}
}