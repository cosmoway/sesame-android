package net.cosmoway.sesame

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.IBinder
import android.os.RemoteException
import android.os.StrictMode
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
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetAddress.getAllByName
import java.net.UnknownHostException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.jmdns.*

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
        val DNS_TYPE = "_irkit._tcp"
        val intentFilter: IntentFilter = IntentFilter()
        var jmdns: JmDNS? = null
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
        /*object : AsyncTask<Void?, Void?, String?>() {
            override fun doInBackground(vararg params: Void?): String? {
                var result: String
                // リクエストオブジェクトを作って
                val request: Request = Request.Builder().url(mUrl).get().head().build()

                // クライアントオブジェクトを作って
                val client: OkHttpClient = OkHttpClient()

                // リクエストして結果を受け取って
                try {
                    val response = client.newCall(request).execute()
                    result = response.body().string()
                } catch (e: IOException) {
                    e.printStackTrace()
                    result = "Connection Error"
                }

                // 返す
                return result
            }

            override fun onPostExecute(result: String?) {
                if (result != null) {
                    Log.d("Log", result)
                    if (result == "200 OK") {
                        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, uri)
                        ringtone.play()
                        try {
                            // レンジング停止
                            mBeaconManager?.stopRangingBeaconsInRegion(mRegion)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                    }
                    makeNotification(result)
                }
            }
        }.execute()*/
    }

    private fun createJmDns() {
        // マルチキャストアドレス宛てパケットを受け取る
        val wifiManager: WifiManager = getSystemService(android.content.Context.WIFI_SERVICE)
                as WifiManager
        // デバッグのためのタグを付加する
        val multiCastLock: WifiManager.MulticastLock = wifiManager.createMulticastLock("for JmDNS")
        multiCastLock.setReferenceCounted(true)
        multiCastLock.acquire()
        if (android.os.Build.VERSION.SDK_INT > 9) {
            val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        try {
            jmdns = JmDNS.create()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "JmDnsError")
        }

        jmdns?.addServiceListener("_http._tcp.local.", object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                jmdns?.requestServiceInfo(event.type, event.name)
                Log.d(TAG, "Service added   : " + event.name + "." + event.type)
                //InetAddress.getAllByName("sesame.local")
            }

            override fun serviceRemoved(event: ServiceEvent) {
                Log.d(TAG, "Service removed : " + event.name + "." + event.type)
            }

            override fun serviceResolved(event: ServiceEvent) {
                val info: ServiceInfo = event.info
                // ipv4アドレスの取得
                val addresses: Array<Inet4Address> = info.inet4Addresses
                Log.d(TAG, "Service resolved: " + info)
                //Log.d(TAG, "Service resolved: " + event.info)
                Log.d(TAG, "Service resolved: " + Arrays.toString(addresses))
            }
        })
        jmdns?.addServiceTypeListener(object : ServiceTypeListener {

            override fun serviceTypeAdded(event: ServiceEvent?) {
                Log.d(TAG, "ServiceType added : " + event?.type)
            }

            override fun subTypeForServiceTypeAdded(event: ServiceEvent?) {
                //Log.d(TAG, "Service added : " + event?.type + "," + event?.info)
            }
        })
        /*var host: InetAddress? = null
        try {
            host = InetAddress.getByName("sesame.local")
            Log.d(TAG, "Host name = " + host?.hostName)
            Log.d(TAG, "IP = " + host?.hostAddress)
        } catch (e: UnknownHostException) {
            Log.d(TAG, "Not found " + "sesame.local")
            return
        }*/
    }

    private fun makeNotification(result: String) {

        /*val builder = NotificationCompat.Builder(applicationContext)
        builder.setSmallIcon(R.mipmap.ic_launcher)

        // メッセージをクリックした時のインテントを作成する
        val notificationIntent = Intent(this@SesameBeaconService, Notification::class.java)
        val contentIntent = PendingIntent.getActivity(this@SesameBeaconService, 0,
                notificationIntent, 0)

        builder.setContentTitle(result) // 1行目
        if (result == "200 OK") {
            builder.setContentText("正常に解錠されました。")
        } else if (result == "Connection Error") {
            builder.setContentText("通信処理が正常に終了されませんでした。\n通信環境を御確認下さい。")
        } else if (result.indexOf("400") != -1) {
            builder.setContentText("予期せぬエラーが発生致しました。\n開発者に御問合せ下さい。")
        } else if (result.indexOf("403") != -1) {
            builder.setContentText("認証に失敗致しました。\nシステム管理者に登録を御確認下さい。")
        }
        builder.setContentIntent(contentIntent)
        builder.setTicker("Sesame") // 通知到着時に通知バーに表示(4.4まで)
        // 5.0からは表示されない

        val manager = NotificationManagerCompat.from(applicationContext)
        manager.notify(1, builder.build())*/

    }

    private fun sendBroadCast(state: Array<String>) {
        val broadcastIntent: Intent = Intent()
        broadcastIntent.putExtra("state", state)
        broadcastIntent.action = "UPDATE_ACTION"
        baseContext.sendBroadcast(broadcastIntent)
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
        //val identifier: Identifier = Identifier.parse("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        Log.d("id", mId)

        // Beacon名の作成
        val beaconId = this@SesameBeaconService.packageName
        // major, minorの指定はしない
        //mRegion = Region(beaconId, identifier, null, null)
        mRegion = Region(beaconId, null, null, null)
        mRegionBootstrap = RegionBootstrap(this, mRegion)
        // iBeacon領域を監視(モニタリング)するスキャン間隔を設定
        mBeaconManager?.setForegroundScanPeriod(1000)
        mBeaconManager?.setBackgroundScanPeriod(1000)

        // Set DNS results to be cached.
        java.security.Security.setProperty("networkaddress.cache.ttl", "1")
        // Do not cache un-successful name lookups from the DNS.
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "1")
        createJmDns()
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

        // 検出したビーコンの情報を全部みる
        //val lastDistance: Double = Double.MAX_VALUE
        //var nearBeacon: Beacon? = null

        beacons?.forEach { beacon ->
            /*if (lastDistance > beacon.distance) {
                nearBeacon = beacon
            }*/

            // ログの出力
            Log.d("Beacon", "UUID:" + beacon.id1 + ", major:" + beacon.id2 + ", minor:" + beacon.id3
                    + ", Distance:" + beacon.distance + "m"
                    + ", RSSI:" + beacon.rssi + ", txPower:" + beacon.txPower)


            //暗号化
            //val safetyPassword1: String = toEncryptedHashValue("SHA-256", mId + "|"
            //        + beacon.id2 + "|" + beacon.id3)
            //URL
            //mUrl = "http://10.0.0.3:10080/?data=" + safetyPassword1
            //mUrl = "http://sesame.local:10080/?data=" + safetyPassword1
            //mUrl = "http://10.0.0.44:10080/?data=" + safetyPassword1
            if (beacon.distance < 3.0) {
                //getRequest()
            }
            val list: Array<String> = arrayOf(beacon.id1.toString(), beacon.id2.toString(), beacon.id3.toString(),
                    beacon.distance.toString(), beacon.rssi.toString(), beacon.txPower.toString()/*, mUrl.toString()*/)
            sendBroadCast(list)
        }
    }

    // 領域に対する状態が変化
    override fun didDetermineStateForRegion(i: Int, region: Region) {
        Log.d(TAG, "Determine State: " + i)
    }
}