package com.tirthdalwadi.weatherquery.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cityName")
data class CityName(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val city: String
)
