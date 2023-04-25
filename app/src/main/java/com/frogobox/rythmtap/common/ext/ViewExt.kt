package com.frogobox.rythmtap.common.ext

import android.content.Context
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Faisal Amir on 24/10/22
 * -----------------------------------------
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * -----------------------------------------
 * Copyright (C) Frogobox ID / amirisback
 * All rights reserved
 */


fun vibrate(context: Context, power: Long) {
    val v = context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
    v.vibrate(power) // ready to rumble!
}

fun View.setOnClickListenerExt(onClick: (v: View) -> Unit) {
    vibrate(context, 30)
    setOnClickListener { onClick(it) }
}