package com.example.automatize.view.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.automatize.R
import com.example.automatize.repository.PrefsConfig

class SettingActivity : AppCompatActivity() {

    private val prefs = PrefsConfig()
    private val protocol = "tcp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val oldDns = removeProtocolFromSharedPreferences( prefs.getValueOfPreferences( PrefsConfig.GLOBAL_IP_KEY_NAME ))
        val oldIp = removeProtocolFromSharedPreferences( prefs.getValueOfPreferences( PrefsConfig.LOCAL_IP_KEY_NAME ))
        val oldUser = prefs.getValueOfPreferences( PrefsConfig.USER_KEY_NAME )
        val oldUserPassword = prefs.getValueOfPreferences( PrefsConfig.PASSWORD_KEY_NAME )
        val oldPort = prefs.getValueOfPreferences( PrefsConfig.PORT_KEY_NAME )

        val dnsView = findViewById<EditText>(R.id.editText_dnsServer)
        dnsView.setText(oldDns)
        val serverIpView = findViewById<EditText>(R.id.editTextServer)
        serverIpView.setText(oldIp)
        val portView = findViewById<EditText>(R.id.editTextPort)
        portView.setText(oldPort)
        val userView = findViewById<EditText>(R.id.editText_user)
        val userPasswordView = findViewById<EditText>(R.id.editText_userPassword)

        val btnSave = findViewById<Button>(R.id.btnSalvar)
        btnSave.setOnClickListener {
            val userPassword = userPasswordView.text.toString()
            val serverIp = serverIpView.text.toString()
            val user = userView.text.toString()
            val port = portView.text.toString()
            val dns = dnsView.text.toString()
            val map = mutableMapOf<String, String>()

            if (port != oldPort && port != "") {
                map[PrefsConfig.PORT_KEY_NAME] = port
            }

            if (serverIp != oldIp && serverIp != "") {
                map[PrefsConfig.LOCAL_IP_KEY_NAME] = "$protocol://$serverIp"
            }

            if (dns != oldDns && dns != "") {
                map[PrefsConfig.GLOBAL_IP_KEY_NAME] = "$protocol://$dns"
            }

            if (user != oldUser && user != "") {
                map[PrefsConfig.USER_KEY_NAME] = user
            }

            if (userPassword != oldUserPassword && userPassword != "") {
                map[PrefsConfig.PASSWORD_KEY_NAME] = userPassword
            }

            prefs.saveOnPreferences(map)
            finish()
        }
    }

    /**
     * @param text O texto que será separado
     * @return O texto enviado sem o prefixo tcp://
     */
    private fun removeProtocolFromSharedPreferences(text: String): String {
        return text.removePrefix("$protocol://")
    }
}