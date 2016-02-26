package net.cosmoway.sesame

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.altbeacon.beacon.BeaconManager

class MainActivity : AppCompatActivity() {

    private var mBeaconManager: BeaconManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //BTMのインスタンス化
        mBeaconManager = BeaconManager.getInstanceForApplication(this)
    }
}
