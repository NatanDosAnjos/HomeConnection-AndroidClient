package com.example.automatize.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager

class PrefsConfig {

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
        val DNS_KEY_NAME = "dns"
    }

    /**
     * @param context O contexto para instanciar uma SharedPreference
     * @param map Os valores contidos nesse mapa são equivalentes aos valores key e value do sharedPreferences
     */
    fun saveOnPreferences(context: Context, map: Map<String, String>) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        for ((key, value) in map) {
            editor.putString(key, value)
        }
        editor.apply()
    }

    /**
     * @param context O contexto para instanciar uma Shared Preference
     * @param key O nome da referência que deseja recuperar
     */
    fun getValueOfPreferences(context: Context, key: String): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, null)?: ""
    }



    /**
     * @param context O contexto para instanciar uma Shared Preference
     * @param listener O callback que irá ser chamado
     */
    fun registerListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }
}