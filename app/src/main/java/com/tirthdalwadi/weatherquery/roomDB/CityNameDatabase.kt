package com.tirthdalwadi.weatherquery.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CityName::class], version = 1)
abstract class CityNameDatabase : RoomDatabase(){
    abstract fun getCityNameDao() : CityNameDAO

    companion object{
        @Volatile
        var INSTANCE: CityNameDatabase? = null

        fun getDatabase(context: Context): CityNameDatabase {
            if(INSTANCE == null)
            {
                synchronized(this){
                    INSTANCE = Room.databaseBuilder(context,
                        CityNameDatabase::class.java,
                        "cityNameDB").build()
                }
            }
            return INSTANCE!!
        }
    }
}




