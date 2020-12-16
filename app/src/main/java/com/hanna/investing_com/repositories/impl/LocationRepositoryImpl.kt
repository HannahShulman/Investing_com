package com.hanna.investing_com.repositories.impl

import android.location.Location
import com.hanna.investing_com.entities.Response
import com.hanna.investing_com.entities.SubInterestPlace
import com.hanna.investing_com.network.Api
import com.hanna.investing_com.network.ApiResponse
import com.hanna.investing_com.network.FlowNetworkOnlyResource
import com.hanna.investing_com.network.Resource
import com.hanna.investing_com.repositories.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class LocationRepositoryImpl @Inject constructor(val api: Api) : LocationRepository {
    override fun getPlacesOfInterest(currentLocation: Location): Flow<Resource<List<SubInterestPlace>>> {

        return object : FlowNetworkOnlyResource<List<SubInterestPlace>, Response>() {
            override fun processResponse(response: Response): List<SubInterestPlace> {
                return response.results.map { SubInterestPlace(it.name, it.geometry.location) }
            }

            override suspend fun saveNetworkResult(item: Response) {

            }

            override suspend fun fetchFromNetwork(): Flow<ApiResponse<Response>> {
                return api.getPlaces("${currentLocation.latitude},${currentLocation.longitude}")
            }
        }.asFlow()
    }

}