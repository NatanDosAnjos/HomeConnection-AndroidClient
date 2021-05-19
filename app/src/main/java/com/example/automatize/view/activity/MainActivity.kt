package com.example.automatize.view.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.automatize.R
import com.example.automatize.view.activity.adapter.MainAdapter
import com.example.automatize.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_view_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var mAdapter: MainAdapter = MainAdapter(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.getDevices().observe(this, {
            mAdapter.updateList(it)
        })

        mAdapter.getDeviceToCommand().observe(this, {
            viewModel.changeStatus(it)
        })
    }


    override fun onStart() {
        super.onStart()

        recyclerView.apply {
            setHasFixedSize(true)
            adapter = mAdapter
            layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager(this@MainActivity, 3)
            } else {
                GridLayoutManager(this@MainActivity, 2)
            }
        }

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