package net.cosmoway.sesame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message

/* Receiver内*/
class SesameBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val bundle = intent.extras
        val state = bundle.getStringArray("state")

        if (sHandler != null) {
            val msg = Message()
            val data = Bundle()
            data.putStringArray("state", state)
            msg.data = data
            sHandler!!.sendMessage(msg)
        }
    }

    /**
     * メイン画面の表示を更新
     */
    fun registerHandler(locationUpdateHandler: Handler) {
        sHandler = locationUpdateHandler
    }

    companion object {
        var sHandler: Handler? = null
    }
}