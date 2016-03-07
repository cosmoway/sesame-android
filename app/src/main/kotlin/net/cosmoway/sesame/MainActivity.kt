package net.cosmoway.sesame

import android.Manifest
import android.app.Activity
import android.app.ListActivity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.NotificationCompat
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView


class MainActivity : ListActivity() {

    //スリープモードからの復帰の為のフラグ定数
    private val FLAG_KEYGUARD = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    private var mReceiver: SesameBroadcastReceiver? = null
    private var mIntentFilter: IntentFilter? = null
    private var mMessage: TextView? = null

    // サービスから値を受け取ったら動かしたい内容を書く
    private val updateHandler = object : Handler() {
        override fun handleMessage(msg: Message) {

            val bundle = msg.data
            val message: Array<String> = bundle.getStringArray("state")
            val lv: ListView = findViewById(R.id.list1) as ListView
            val adapter: ArrayAdapter<String> = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, message)
            listAdapter = adapter
            lv.adapter
        }
    }

    private fun requestLocationPermission() {
        // 位置情報サーヴィス を利用可能か
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //許可を求めるダイアログを表示します。
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestLocationPermission()

        /*val manager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo
        val apInfo = arrayOfNulls<String>(4)
        //SSIDを取得
        apInfo[0] = String.format("SSID : %s", info.ssid)
        // IPアドレスを取得
        val ipAdr = info.ipAddress
        apInfo[1] = String.format("IP Adrress : %02d.%02d.%02d.%02d",
                ipAdr and 0xff, ipAdr shr 8 and 0xff, ipAdr shr 16 and 0xff, ipAdr shr 24 and 0xff)
        // MACアドレスを取得
        apInfo[2] = String.format("MAC Address : %s", info.macAddress)
        // 受信信号強度&信号レベルを取得
        val rssi = info.rssi
        val level = WifiManager.calculateSignalLevel(rssi, 5)
        apInfo[3] = String.format("RSSI : %d / Level : %d/4", rssi, level)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, apInfo)
        listAdapter = adapter*/
        // サービスの開始
        //permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            startService(Intent(this, SesameBeaconService::class.java))
        }

        mReceiver = SesameBroadcastReceiver()
        mIntentFilter = IntentFilter()
        (mIntentFilter as IntentFilter).addAction("UPDATE_ACTION")
        registerReceiver(mReceiver, mIntentFilter);

        (mReceiver as SesameBroadcastReceiver).registerHandler(updateHandler);
    }

    override fun onStop() {
        super.onStop()
        window.clearFlags(FLAG_KEYGUARD)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(FLAG_KEYGUARD)
    }

    fun notification(result: String) {
        val builder = NotificationCompat.Builder(applicationContext)
        builder.setSmallIcon(R.mipmap.ic_launcher)

        // メッセージをクリックした時のインテントを作成する
        val notificationIntent = Intent(this, Notification::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        builder.setContentTitle(result) // 1行目
        builder.setContentText("解錠されました。")
        builder.setContentIntent(contentIntent)
        builder.setTicker("Ticker") // 通知到着時に通知バーに表示(4.4まで)
        // 5.0からは表示されない

        val manager = NotificationManagerCompat.from(applicationContext)
        manager.notify(1, builder.build())
    }
}
