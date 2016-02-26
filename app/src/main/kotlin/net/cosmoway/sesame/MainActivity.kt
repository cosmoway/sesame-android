package net.cosmoway.sesame

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser

class MainActivity : AppCompatActivity() {

    private var mBeaconManager: BeaconManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //BTMのインスタンス化
        mBeaconManager = BeaconManager.getInstanceForApplication(this)

        //Parserの設定
        val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))

        
    }
}
