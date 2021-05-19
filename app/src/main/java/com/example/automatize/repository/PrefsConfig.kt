package com.example.automatize.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PrefsConfig: KoinComponent {
    private val context: Context by inject()

    /**
     * Contem os nomes das keys principais no shared Preference padrão
     * de forma encapsulada
     */
    companion object {
        @JvmStatic
        val PASSWORD_KEY_NAME = "password"

        @JvmStatic
        val LOCAL_IP_KEY_NAME = "ip"

        @JvmStatic
        val USER_KEY_NAME = "user"

        @JvmStatic
        val PORT_KEY_NAME = "port"

        @JvmStatic
        val GLOBAL_IP_KEY_NAME = "dns"

        @JvmStatic
        val PROTOCOL_PREFIX = "tcp://"
    }

    /**
     * @param map Os valores contidos nesse mapa são equivalentes aos valores key e value do sharedPreferences
     */
    fun saveOnPreferences(map: Map<String, String>) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        for ((key, value) in map) {
            editor.putString(key, value)
        }
        editor.apply()
    }

    /**
     * @param key O nome da referência que deseja recuperar
     */
    fun getValueOfPreferences(key: String): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, null)?: ""
    }

    fun getPreferencesIpWithoutPrefix(key: String): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val pureText = prefs.getString(key, null) ?: ""
        return if(pureText.isEmpty()) {
            ""
        } else {
            pureText.removePrefix("$PROTOCOL_PREFIX://")
        }
    }



    /**
     * @param listener O callback que irá ser chamado
     */
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }
}