package com.tirthdalwadi.weatherquery

import android.app.Activity
import android.app.AlertDialog

class Loading (private val mActivity: Activity){
    private lateinit var isDialog: AlertDialog
    fun startLoading()
    {
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading, null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.show()
    }

    fun dismissLoading()
    {
        isDialog.dismiss()
    }
}