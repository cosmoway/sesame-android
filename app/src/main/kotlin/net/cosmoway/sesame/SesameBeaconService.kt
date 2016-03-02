package net.cosmoway.sesame

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.IBinder
import android.os.RemoteException
import android.preference.PreferenceManager
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.altbeacon.beacon.*
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

// BeaconServiceクラス
class SesameBeaconService : Service(), BeaconConsumer, BootstrapNotifier, RangeNotifier,
        MonitorNotifier {
    // BGで監視するiBeacon領域
    private var mRegionBootstrap: RegionBootstrap? = null
    // iBeacon検知用のマネージャー
    private var mBeaconManager: BeaconManager? = null
    // UUID設定用
    private var mId: String? = null
    // iBeacon領域
    private var mRegion: Region? = null
    // URL
    private var mUrl: String? = null

    companion object {
        val TAG = org.altbeacon.beacon.service.BeaconService::class.java.simpleName
    }

    private fun toEncryptedHashValue(algorithmName: String, value: String): String {
        var md: MessageDigest? = null
        try {
            md = MessageDigest.getInstance(algorithmName)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        val sb: StringBuilder = StringBuilder()
        md!!.update(value.toByteArray())
        for (b in md.digest()) {
            val hex = String.format("%02x", b)
            sb.append(hex)
        }
        return sb.toString()
    }

    private fun getRequest() {
        object : AsyncTask<Void?, Void?, String?>() {
            override fun doInBackground(vararg params: Void?): String? {
                var result: String

                // リクエストオブジェクトを作って
                val request = Request.Builder().url(mUrl).get().build()

                // クライアントオブジェクトを作って
                val client = OkHttpClient()

                // リクエストして結果を受け取って
                try {
                    val response = client.newCall(request).execute()
                    result = response.body().string()
                    return result
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("response", "error")
                }

                // 返す
                return null
            }

            override fun onPostExecute(result: String?) {
                if (result != null) {
                    Log.d("Log", result)
                    if (result == "200 OK") {
                        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, uri);
                        ringtone.play()

                        val builder = NotificationCompat.Builder(applicationContext)
                        builder.setSmallIcon(R.mipmap.ic_launcher)

                        // メッセージをクリックした時のインテントを作成する
                        val notificationIntent = Intent(this@SesameBeaconService, Notification::class.java)
                        val contentIntent = PendingIntent.getActivity(this@SesameBeaconService, 0,
                                notificationIntent, 0)

                        builder.setContentTitle(result) // 1行目
                        builder.setContentText("解錠されました。")
                        builder.setContentIntent(contentIntent)
                        builder.setTicker("Ticker") // 通知到着時に通知バーに表示(4.4まで)
                        // 5.0からは表示されない

                        val manager = NotificationManagerCompat.from(applicationContext)
                        manager.notify(1, builder.build())
                    } else {
                        val builder = NotificationCompat.Builder(applicationContext)
                        builder.setSmallIcon(R.mipmap.ic_launcher)

                        // メッセージをクリックした時のインテントを作成する
                        val notificationIntent = Intent(this@SesameBeaconService, Notification::class.java)
                        val contentIntent = PendingIntent.getActivity(this@SesameBeaconService, 0,
                                notificationIntent, 0)

                        builder.setContentTitle(result) // 1行目
                        builder.setContentText("この端末は認証されていない可能性がございます。\nシステム管理者にお問合せ下さい。") // 400（403：ネットワークに正常に接続出来ませんでした。）
                        builder.setContentIntent(contentIntent)
                        builder.setTicker("Ticker") // 通知到着時に通知バーに表示(4.4まで)
                        // 5.0からは表示されない

                        val manager = NotificationManagerCompat.from(applicationContext)
                        manager.notify(1, builder.build())
                    }
                }
            }
        }.execute()
    }

    override fun onCreate() {
        super.onCreate()
        //BTMのインスタンス化
        mBeaconManager = BeaconManager.getInstanceForApplication(this)

        //Parserの設定
        val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))

        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        // 端末固有識別番号読出
        mId = sp.getString("SaveString", null)
        if (mId == null) {
            Log.d("id", "null")
            // 端末固有識別番号取得
            mId = UUID.randomUUID().toString()
            // 端末固有識別番号記憶
            sp.edit().putString("SaveString", mId).apply()
        }
        //val identifier: Identifier = Identifier.parse(mId)
        Log.d("id", mId)

        // Beacon名の作成
        val beaconId = "ando"
        // major, minorの指定はしない
        //mRegion = Region(beaconId, identifier, null, null)
        mRegion = Region(beaconId, null, null, null)
        mRegionBootstrap = RegionBootstrap(this, mRegion)
        // iBeacon領域を監視(モニタリング)するスキャン間隔を設定
        mBeaconManager?.setForegroundBetweenScanPeriod(1000)
        mBeaconManager?.setBackgroundBetweenScanPeriod(1000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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
    }

    // 領域進入
    override fun didEnterRegion(region: Region) {
        Log.d(TAG, "Enter Region")

        // アプリをFG起動させる
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        // レンジング開始
        try {
            mBeaconManager?.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        //Beacon情報の取得
        mBeaconManager?.setRangeNotifier(this)
    }

    // 領域退出
    override fun didExitRegion(region: Region) {
        Log.d(TAG, "Exit Region")

        // レンジング停止
        try {
            mBeaconManager?.stopRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        beacons?.forEach { beacon ->
            // ログの出力
            Log.d("Beacon", "UUID:" + beacon.id1 + ", major:" + beacon.id2 + ", minor:" + beacon.id3
                    + ", Distance:" + beacon.distance + "m" + ", RSSI:" + beacon.rssi)
            //暗号化
            val safetyPassword1: String = toEncryptedHashValue("SHA-256", mId + "|"
                    + beacon.id2 + "|" + beacon.id3)

            //URL
            mUrl = "http://10.0.0.3:10080/?data=" + safetyPassword1
            if (beacon.distance < 1) {
                getRequest()
            }
        }
    }

    // 領域に対する状態が変化
    override fun didDetermineStateForRegion(i: Int, region: Region) {
        Log.d(TAG, "Determine State: " + i)
    }
}