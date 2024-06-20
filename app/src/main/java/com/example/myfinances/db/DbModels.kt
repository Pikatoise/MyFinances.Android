package com.example.myfinances.db

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class Operation(var id: Int, var type: Int,var title: String,var amount: Double,var periodId: Int):
	Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readInt(),
		parcel.readString() as String,
		parcel.readDouble(),
		parcel.readInt()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(id)
		parcel.writeInt(type)
		parcel.writeString(title)
		parcel.writeDouble(amount)
		parcel.writeInt(periodId)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<Operation> {
		override fun createFromParcel(parcel: Parcel): Operation {
			return Operation(parcel)
		}

		override fun newArray(size: Int): Array<Operation?> {
			return arrayOfNulls(size)
		}
	}
}

data class Period(var id: Int, var year: Int, var month: Int, var isCurrent: Boolean):
	Parcelable {
	@RequiresApi(Build.VERSION_CODES.Q)
	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readInt(),
		parcel.readInt(),
		parcel.readBoolean()
	)

	@RequiresApi(Build.VERSION_CODES.Q)
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(id)
		parcel.writeInt(year)
		parcel.writeInt(month)
		parcel.writeBoolean(isCurrent)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<Period> {
		@RequiresApi(Build.VERSION_CODES.Q)
		override fun createFromParcel(parcel: Parcel): Period {
			return Period(parcel)
		}

		override fun newArray(size: Int): Array<Period?> {
			return arrayOfNulls(size)
		}
	}
}

data class Plan(var id: Int, var name: String, var date: String, var typePath: String, var status: Boolean):
	Parcelable {
	@RequiresApi(Build.VERSION_CODES.Q)
	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readString() as String,
		parcel.readString() as String,
		parcel.readString() as String,
		parcel.readBoolean()
	)

	@RequiresApi(Build.VERSION_CODES.Q)
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(id)
		parcel.writeString(name)
		parcel.writeString(date)
		parcel.writeString(typePath)
		parcel.writeBoolean(status)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<Plan> {
		@RequiresApi(Build.VERSION_CODES.Q)
		override fun createFromParcel(parcel: Parcel): Plan {
			return Plan(parcel)
		}

		override fun newArray(size: Int): Array<Plan?> {
			return arrayOfNulls(size)
		}
	}
}