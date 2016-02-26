package net.cosmoway.sesame

import android.app.Application
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.startup.BootstrapNotifier

import org.altbeacon.beacon.startup.RegionBootstrap

/**
 * Created by ando on 16/02/26.
 * a
 */
class BeaconApplication : Application(), BootstrapNotifier {

    private var regionBootstrap: RegionBootstrap? = null

    companion object {

        val TAG = BeaconApplication::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun didEnterRegion(p0: Region?) {
        // 領域に入場した
    }

    override fun didExitRegion(p0: Region?) {
        // 領域から退場した
    }

    override fun didDetermineStateForRegion(p0: Int, p1: Region?) {
        // 入退場状態が変更された
    }
}