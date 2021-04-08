package com.mtkreader.views.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.mtkreader.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

}