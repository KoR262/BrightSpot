package com.example.brightspot

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

data class Photo(val userId: String? = null, val photoUrl: String? = null, val latLng: LatLng? = null)
