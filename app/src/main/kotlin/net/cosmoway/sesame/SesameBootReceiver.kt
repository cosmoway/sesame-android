package net.cosmoway.sesame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SesameBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(Intent(context, SesameBeaconService::class.java))
        }
    }
}
