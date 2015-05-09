package pt.ulisboa.tecnico.cmov.cmovproject.connectivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;

public class ConnectivityService extends Service {
    private boolean wiFiDirectIsOn = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        if(!wiFiDirectIsOn) {
            toggleWiFiDirect();
        }

        // TODO: get list of users from intent. Return through the intent the list of online users.

        return START_STICKY;
    }

    public boolean toggleWiFiDirect(){
        if(wiFiDirectIsOn) {
            unbindService(mConnection);
            return wiFiDirectIsOn = false;
        } else {
            Intent startIntent = new Intent(this, SimWifiP2pService.class);
            bindService(startIntent, mConnection, Context.BIND_AUTO_CREATE);
            return wiFiDirectIsOn = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Can not bind to this service");
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Messenger mService = new Messenger(service);
            SimWifiP2pManager mManager = new SimWifiP2pManager(mService);
            mManager.initialize(getApplication(), getMainLooper(), null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
