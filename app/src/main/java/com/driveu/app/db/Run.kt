package com.driveu.app.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "running_table")
data class Run(
    var img: String? =null ,
    var timestamp: Long? = null,
    var avgSpeedInKMH: Float? = null,
    var distanceInMeters: Int? = null,
    var timeInMillis: Long ?= null,
    var polylines: ArrayList<ArrayList<LatLngData>>?=null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

data class LatLngData(val latitude: Double, val longitude: Double)

