package com.example.automatize.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automatize.R
import com.example.automatize.model.ServerToConnect
import com.example.automatize.view.activity.adapter.MainAdapter
import com.example.automatize.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by  viewModel()
    private lateinit var mAdapter: MainAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAdapter = MainAdapter()
        ServerToConnect.context = this
    }

    override fun onStart() {
        super.onStart()
        mAdapter.getDeviceToCommand().observe(this, {
            viewModel.changeStatus(it)
        })

        recyclerView.apply {
            setHasFixedSize(true)
            adapter = mAdapter
            layoutManager = LinearLayoutManager(applicationContext)
            layoutManager?.canScrollHorizontally()
        }

        viewModel.getDevices().observe(this, {
            println(it)
            mAdapter.updateList(it)
        })
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