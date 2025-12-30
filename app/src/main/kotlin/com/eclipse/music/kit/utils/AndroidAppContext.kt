package com.eclipse.music.kit.utils

import android.content.Context

object AndroidAppContext {
    var applicationContext: Context? = null
        private set

    fun init(ctx: Context) {
        applicationContext = ctx.applicationContext
    }
}