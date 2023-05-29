package com.tirthdalwadi.weatherquery.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CityNameDAO {

    @Insert
    suspend fun insertCityName(city: CityName)

    @Delete
    suspend fun deleteCityName(city: CityName)

    @Update
    suspend fun updateCityName(city: CityName)

    @Query("DELETE FROM cityName")
    suspend fun deleteAll()

    @Query("SELECT * FROM cityName")
    fun getCityName(): List<CityName>
}