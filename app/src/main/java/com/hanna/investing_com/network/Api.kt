package com.hanna.investing_com.network

import com.hanna.investing_com.entities.Response
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query


//Prototypes - N/A
//Tests - N/A
interface Api {

    @GET("place/nearbysearch/json?rankby=distance")
    fun getPlaces(@Query("location") location: String): Flow<ApiResponse<Response>>
}