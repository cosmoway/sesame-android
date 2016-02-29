package net.cosmoway.sesame

import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import org.altbeacon.beacon.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity(), BeaconConsumer, MonitorNotifier, RangeNotifier {

    private var mBeaconManager: BeaconManager? = null
    private var mRegion: Region? = null

    private fun toEncryptedHashValue(algorithmName: String, value: String): String {
        var md: MessageDigest? = null
        var sb: StringBuilder? = null
        try {
            md = MessageDigest.getInstance(algorithmName)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        md!!.update(value.toByteArray())
        sb = StringBuilder()
        for (b in md.digest()) {
            val hex = String.format("%02x", b)
            sb.append(hex)
        }
        return sb.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //BTMのインスタンス化
        mBeaconManager = BeaconManager.getInstanceForApplication(this)

        //Parserの設定
        val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))

        //とりあえずbeacon全部認識するように
        val telephonyManager: TelephonyManager = getSystemService(Context.TELEPHONY_SERVICE)
                as TelephonyManager
        val id: String = telephonyManager.deviceId
        mRegion = Region(id, null, null, null)

        //暗号化
        val safetyPassword1: String = toEncryptedHashValue("SHA-256", id)
        Log.d("id", safetyPassword1)

    }

    //Beaconサービスの接続と開始
    override fun onBeaconServiceConnect() {
        //領域監視の設定
        mBeaconManager?.setMonitorNotifier(this)
        try {
            // ビーコン情報の監視を開始
            mBeaconManager?.startMonitoringBeaconsInRegion(mRegion)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        //Beacon情報の取得
        mBeaconManager?.setRangeNotifier(this)
    }

    override fun onResume() {
        super.onResume()
        mBeaconManager?.bind(this) // サービスの開始

    }

    override fun onPause() {
        super.onPause()
        mBeaconManager?.unbind(this) // サービスの停止
    }

    override fun didEnterRegion(region: Region?) {
        // 領域への入場を検知
        mBeaconManager?.startRangingBeaconsInRegion(mRegion)
        Log.d("Beacon", "ENTER")
    }

    override fun didExitRegion(region: Region?) {
        // 領域からの退場を検知
        Log.d("Beacon", "EXIT")
        mBeaconManager?.stopRangingBeaconsInRegion(mRegion)
    }

    override fun didDetermineStateForRegion(i: Int, region: Region?) {
        // 領域への入退場のステータス変化を検知
        Log.d("Beacon", "DetermineState: " + i)
    }

    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        beacons?.forEach { beacon ->
            // ログの出力
            Log.d("Beacon", "UUID:" + beacon.id1 + ", major:" + beacon.id2
                    + ", minor:" + beacon.id3 + ", Distance:" + beacon.distance + "m"
                    + ",RSSI" + beacon.rssi)
        }
    }
}
