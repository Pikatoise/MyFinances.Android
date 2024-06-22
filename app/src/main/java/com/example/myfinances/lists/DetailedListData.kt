package com.example.myfinances.lists

import com.example.myfinances.api.models.period.PeriodResponse

data class DetailedListData (
	var period: PeriodResponse,
	var operations: ArrayList<DetailedListItemData>
)