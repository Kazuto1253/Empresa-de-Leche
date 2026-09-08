package pe.gob.huata.ecolactea.shared.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import pe.gob.huata.ecolactea.core.application.sync.ConnectivityEvents
import pe.gob.huata.ecolactea.core.application.sync.ConnectivityMonitor
import pe.gob.huata.ecolactea.core.application.sync.ConnectivityStatus

class AndroidConnectivityEvents(context: Context) : ConnectivityEvents, ConnectivityMonitor {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var callback: ConnectivityManager.NetworkCallback? = null

    override suspend fun currentStatus(): ConnectivityStatus {
        val network = manager.activeNetwork ?: return ConnectivityStatus.OFFLINE
        val capabilities = manager.getNetworkCapabilities(network) ?: return ConnectivityStatus.OFFLINE
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) ConnectivityStatus.ONLINE else ConnectivityStatus.OFFLINE
    }

    override fun start(onOnline: () -> Unit) {
        if (callback != null) return
        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) onOnline()
            }
        }.also { manager.registerNetworkCallback(NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), it) }
    }

    override fun stop() {
        callback?.let(manager::unregisterNetworkCallback)
        callback = null
    }
}
