package com.hanna.investing_com.repositories

import android.location.Location
import com.hanna.investing_com.entities.SubInterestPlace
import com.hanna.investing_com.network.Resource
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getPlacesOfInterest(currentLocation: Location): Flow<Resource<List<SubInterestPlace>>>
}