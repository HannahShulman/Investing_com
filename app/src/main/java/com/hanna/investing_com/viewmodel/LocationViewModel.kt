package com.hanna.investing_com.viewmodel

import android.location.Location
import androidx.lifecycle.*
import com.hanna.investing_com.entities.SubInterestPlace
import com.hanna.investing_com.network.Resource
import com.hanna.investing_com.repositories.LocationRepository
import javax.inject.Inject

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    val locationMutableLiveData = MutableLiveData<Location?>()

    //trigger new request for nearby points of interest
    val nearbyPointsOfInterest: LiveData<Resource<List<SubInterestPlace>>> =
        Transformations.switchMap(locationMutableLiveData) {
            return@switchMap it?.run { repository.getPlacesOfInterest(it).asLiveData() }
        }

    fun setCurrentLocationValue(currentLocation: Location?) {
        //to ensure request is triggered just on location change.
        locationMutableLiveData.value.let { locationValue ->
            if (locationValue == null || locationValue.latitude != currentLocation?.latitude ||
                locationValue.longitude != currentLocation.longitude
            ) {
                locationMutableLiveData.value = currentLocation
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class LocationViewModelFactory @Inject constructor(private val repository: LocationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LocationViewModel(repository) as T
    }
}