package com.example.automatize.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.preference.PreferenceManager
import com.example.automatize.R
import com.example.automatize.model.ServerToConnect

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val serverView = findViewById<EditText>(R.id.editTextServer)
        val portView = findViewById<EditText>(R.id.editTextPort)
        val btnSave = findViewById<Button>(R.id.btnSalvar)
        val dnsView = findViewById<EditText>(R.id.editText_dnsServer)

        btnSave.setOnClickListener {
            val server = serverView.text.toString()
            val port = portView.text.toString()
            val dns = dnsView.text.toString()

            if (server.isNotEmpty() && port.isNotEmpty()) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPreferences.edit()
                    .putString(ServerToConnect.LOCAL_IP_KEY_NAME, "tcp://$server")
                    .putString(ServerToConnect.DNS_KEY_NAME, "tcp://$dns")
                    .putString(ServerToConnect.PORT_KEY_NAME, port)
                    .putString(ServerToConnect.USER_KEY_NAME, "pi")
                    .putString(ServerToConnect.PASSWORD_KEY_NAME, "D0s@nj0s")
                    .apply()

                finish()
            }

        }

    }
}