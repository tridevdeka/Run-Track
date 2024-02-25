package com.driveu.app.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromArrayList(polylines: ArrayList<ArrayList<LatLngData>>): String {
        val gson = Gson()
        return gson.toJson(polylines)
    }

    @TypeConverter
    fun toArrayList(value: String): ArrayList<ArrayList<LatLngData>> {
        val listType: Type = object : TypeToken<ArrayList<ArrayList<LatLngData>>>() {}.type
        return Gson().fromJson(value, listType)
    }
}