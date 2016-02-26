package net.cosmoway.sesame

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.RemoteException
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region

class MainActivity : AppCompatActivity(), BeaconConsumer {

    private var mBeaconManager: BeaconManager? = null
    private var mRegion: Region? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //BTMのインスタンス化
        mBeaconManager = BeaconManager.getInstanceForApplication(this)

        //Parserの設定
        val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))

        //とりあえずbeacon全部認識するように
        mRegion = Region("unique-id-001", null, null, null)
    }

    override fun onBeaconServiceConnect() {
        try {
            // ビーコン情報の監視を開始
            mBeaconManager?.startMonitoringBeaconsInRegion(mRegion)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        mBeaconManager?.bind(this) // サービスの開始

    }

    override fun onPause() {
        super.onPause()
        mBeaconManager?.unbind(this) // サービスの停止
    }
}
