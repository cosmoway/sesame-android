package net.cosmoway.sesame

import android.app.Application
import android.content.Intent
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.startup.BootstrapNotifier

import org.altbeacon.beacon.startup.RegionBootstrap

/**
 * Created by ando on 16/02/26.
 * a
 */
class BeaconApplication : Application(), BootstrapNotifier {

    private var regionBootstrap: RegionBootstrap? = null
    private var mBeaconManager: BeaconManager? = null

    companion object {

        val TAG = BeaconApplication::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        mBeaconManager = BeaconManager.getInstanceForApplication(this)

        //Parserの設定
        val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))
    }

    override fun didEnterRegion(p0: Region?) {
        // 領域に入場した
        var intent: Intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun didExitRegion(p0: Region?) {
        // 領域から退場した
    }

    override fun didDetermineStateForRegion(p0: Int, p1: Region?) {
        // 入退場状態が変更された
    }
}