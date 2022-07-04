package com.example.brightspot

import com.example.brightspot.adapter.Simple

data class RouteSimple (override val id : String, val userID: String? = null, val distance: Int? = null) : Simple