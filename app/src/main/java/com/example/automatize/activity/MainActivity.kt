package com.example.automatize.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.automatize.R
import com.example.automatize.adapter.MainAdapter
import com.example.automatize.model.Device
import com.example.automatize.model.ServerToConnect
import com.example.automatize.util.changeUi


class MainActivity : AppCompatActivity() {

    private lateinit var server: ServerToConnect
    private lateinit var viewAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        server = ServerToConnect(this, recyclerView)
        server.registerSharedPreferencesListener()
        server.qualityOfServiceArray = intArrayOf(2)
        server.subscribeArray = arrayOf(Device.TOPIC_DEVICES_JSON)

        val viewManager = LinearLayoutManager(this)
        viewAdapter = MainAdapter(ServerToConnect.devicesList, server)

        server.runnable = Runnable {
            changeUi(Runnable {viewAdapter.notifyDataSetChanged()})
        }

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> startActivity(Intent(this, SettingActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

}