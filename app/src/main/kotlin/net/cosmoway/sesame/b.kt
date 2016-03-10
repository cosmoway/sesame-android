package net.cosmoway.sesame

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo

/**
 * Created by ando on 16/03/08.
 */
class b {
    private inner class MyResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            //your code
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            //your code
        }
    }

}
