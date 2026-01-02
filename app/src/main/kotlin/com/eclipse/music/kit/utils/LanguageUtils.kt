package com.eclipse.music.kit.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val code: String) {
    FOLLOW_SYSTEM("auto"),
    ENGLISH("en"),
    CHINESE("zh");

    companion object {
        fun fromCode(code: String) = entries.find { it.code == code } ?: FOLLOW_SYSTEM
    }
}

object LanguageUtils {
    fun attachBaseContext(context: Context, languageCode: String): Context {
        val appLanguage = AppLanguage.fromCode(languageCode)
        if (appLanguage == AppLanguage.FOLLOW_SYSTEM) return context

        val locale = Locale.forLanguageTag(appLanguage.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }
}
