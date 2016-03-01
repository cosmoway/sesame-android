package net.cosmoway.sesame

import android.app.Activity
import android.content.Intent
import android.os.Bundle


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(this@MainActivity, SesameBeaconService::class.java))
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}
