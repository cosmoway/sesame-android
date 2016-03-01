package net.cosmoway.sesame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager


class MainActivity : Activity() {

    //スリープモードからの復帰の為のフラグ定数
    private val FLAG_KEYGUARD = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // サービスの開始
        startService(Intent(this, SesameBeaconService::class.java))
    }

    override fun onStop() {
        super.onStop()
        window.clearFlags(FLAG_KEYGUARD)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(FLAG_KEYGUARD)
    }
}
