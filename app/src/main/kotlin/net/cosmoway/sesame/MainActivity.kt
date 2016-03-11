package net.cosmoway.sesame

import android.Manifest
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView


class MainActivity : ListActivity() {

    //スリープモードからの復帰の為のフラグ定数
    private val FLAG_KEYGUARD = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    private var mReceiver: SesameBroadcastReceiver? = null
    private var mIntentFilter: IntentFilter? = null
    private val REQUEST_PERMISSION: Int = 1;

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

        //電源に関するパーミッション要求。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) run {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            val packageName = packageName
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder(this)
                        .setTitle("確認")
                        .setMessage("本アプリが正常に動作する為には、電池の最適化の解除が必要です。"
                                + "\nなお、最適化状態時は、ビーコンの監視に影響が発生します。")
                        .setNeutralButton("OK") { dialog, which ->
                            val intent = Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = Uri.parse("package:" + packageName)
                            startActivityForResult(intent, REQUEST_PERMISSION)
                        }.show()
            }
        }

        requestLocationPermission()

        //permission check
        val wifiManager: WifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled == false) {
            wifiManager.isWifiEnabled = true
        }

        val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter.isEnabled == false) {
            adapter.enable()
        }

        /*mReceiver = SesameBroadcastReceiver()
        mIntentFilter = IntentFilter()
        (mIntentFilter as IntentFilter).addAction("UPDATE_ACTION")
        registerReceiver(mReceiver, mIntentFilter)

        (mReceiver as SesameBroadcastReceiver).registerHandler(updateHandler)*/
    }

    override fun onStop() {
        super.onStop()
        window.clearFlags(FLAG_KEYGUARD)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(FLAG_KEYGUARD)
        //permission check
        if (ActivityCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        startService(Intent(this, SesameBeaconService::class.java))
        //stopService(Intent(this, SesameBeaconService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregisterReceiver(mReceiver)
    }
}
