package net.cosmoway.sesame

import android.app.Application

import org.altbeacon.beacon.startup.RegionBootstrap

/**
 * Created by ando on 16/02/26.
 * a
 */
class BeaconApplication : Application() {

    private val regionBootstrap: RegionBootstrap? = null

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        val TAG = BeaconApplication::class.java.simpleName
    }
}