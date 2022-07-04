package com.example.brightspot

import com.google.android.gms.maps.model.LatLng

data class Route (val latLngList: List<LatLng>, val userID: String? = null, val distance: Int? = null){
}