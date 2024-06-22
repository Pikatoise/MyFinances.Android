package com.example.myfinances.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.myfinances.lists.DetailedListAdapter
import com.example.myfinances.lists.DetailedListData
import com.example.myfinances.api.repositories.ApiOperationRepository
import com.example.myfinances.api.repositories.ApiOperationTypeRepository
import com.example.myfinances.api.repositories.ApiPeriodRepository
import com.example.myfinances.databinding.ActivityAllOperationsBinding
import com.example.myfinances.db.AccessDataRepository
import com.example.myfinances.lists.DetailedListItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class AllOperationsActivity : AppCompatActivity() {
	private lateinit var binding: ActivityAllOperationsBinding

	private lateinit var apiPeriodRepo: ApiPeriodRepository
	private lateinit var apiOperationRepo: ApiOperationRepository
	private lateinit var apiTypesRepo: ApiOperationTypeRepository
	private lateinit var accessDataRepo: AccessDataRepository


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityAllOperationsBinding.inflate(layoutInflater)

		val sharedPreferencesContext = getSharedPreferences(AccessDataRepository.preferencesName, MODE_PRIVATE)
		accessDataRepo = AccessDataRepository(sharedPreferencesContext)

		val accessToken = accessDataRepo.getAccessToken()!!

		apiPeriodRepo = ApiPeriodRepository(accessToken)
		apiOperationRepo = ApiOperationRepository(accessToken)
		apiTypesRepo = ApiOperationTypeRepository(accessToken)

		binding.ivAllOperationsBackBtn.setOnClickListener{
			finish()
		}

		fillAllOperations()

		setContentView(binding.root)
	}

	private fun fillAllOperations(){
		val detailedDataArrayList = ArrayList<DetailedListData>()

		val requestAllPeriods = CoroutineScope(Dispatchers.Main).async {
			apiPeriodRepo.sendAllPeriodsRequest(accessDataRepo.getUserId()).await()
		}

		requestAllPeriods.invokeOnCompletion {
			val responseAllPeriods = runBlocking { requestAllPeriods.await() }

			if (responseAllPeriods.isSuccessful){
				val periods = responseAllPeriods.success!!.data

				val requestTypes = CoroutineScope(Dispatchers.Main).async {
					apiTypesRepo.sendAllTypesRequest().await()
				}

				requestTypes.invokeOnCompletion {
					val responseTypes = runBlocking { requestTypes.await() }

					if (responseTypes.isSuccessful) {
						val types = responseTypes.success!!.data

						for(i in 0..periods.size - 1){
							val requestOperations = CoroutineScope(Dispatchers.Main).async {
								apiOperationRepo.sendOperationsByPeriodRequest(periods[i].id).await()
							}

							requestOperations.invokeOnCompletion {
								val responseOperations = runBlocking { requestOperations.await() }

								if (responseOperations.isSuccessful){
									val operations = responseOperations.success!!.data
									operations.reverse()

									if (operations.isNotEmpty()){
										detailedDataArrayList.add(
											DetailedListData(periods[i], arrayListOf())
										)

										val currentPeriodPosition = detailedDataArrayList.size - 1

										for (operation in operations){
											val detailedListData = DetailedListItemData(
												types.find { x -> x.id == operation.typeId }!!.iconSrc,
												operation.title,
												operation.amount
											)

											detailedDataArrayList[currentPeriodPosition].operations.add(detailedListData)
										}
									}

									if (i == periods.size - 1){
										if (detailedDataArrayList.isNotEmpty()){
											detailedDataArrayList.reverse()

											binding.tvAllOperationsEmpty.visibility = View.INVISIBLE

											val listAdapter = DetailedListAdapter(this@AllOperationsActivity, detailedDataArrayList)
											binding.lvAllOperations.adapter = listAdapter
										}
										else
											binding.tvAllOperationsEmpty.visibility = View.VISIBLE
									}
								}
							}
						}
					}
				}
			}
			else{
				binding.tvAllOperationsEmpty.visibility = View.VISIBLE

				return@invokeOnCompletion
			}
		}
	}
}