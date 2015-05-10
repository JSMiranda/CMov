package pt.ulisboa.tecnico.cmov.cmovproject.connectivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;

public class ConnectivityService extends Service implements GroupInfoListener {
    boolean mWiFiDirectIsOn = false;
    SimWifiP2pManager mManager = null;
    Channel mChannel = null;

    private TreeMap<String, String> userIps = new TreeMap<String, String>();

    private final String TAG = this.getClass().getName();

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
        SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        if(!mWiFiDirectIsOn) {
            toggleWiFiDirect();
        }

        // TODO: get list of users from intent. Return through the intent the list of online users.

        return START_STICKY;
    }

    public boolean toggleWiFiDirect(){
        if(mWiFiDirectIsOn) {
            unbindService(mConnection);
            return mWiFiDirectIsOn = false;
        } else {
            Intent startIntent = new Intent(this, SimWifiP2pService.class);
            bindService(startIntent, mConnection, Context.BIND_AUTO_CREATE);
            return mWiFiDirectIsOn = true;
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
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mManager = null;
            mChannel = null;
        }
    };

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {

        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        ArrayList<String> peerIps = new ArrayList<String>();
        int i = 0;
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devStr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devStr);

            if(device != null)
                peerIps.add(device.getVirtIp());
        }

        Log.d(TAG, peersStr.toString());
        removeExitingPeersFromUserMap(peerIps);
        addJoiningPeersToUserMap(peerIps);
        Log.d(TAG, "Map changed: " + userIps);
    }

    private void addJoiningPeersToUserMap(final ArrayList<String> peerIps) {
        for(String ip : peerIps) {
            if(!userIps.containsValue(ip)) {
                String userName = askForUsername(ip);
                userIps.put(userName, ip);
            }
        }
    }

    private String askForUsername(String ip) {
        // TODO: Implement
        return "Teste";
    }

    private void removeExitingPeersFromUserMap(ArrayList<String> peerIps) {
        for(Map.Entry<String, String> entry : userIps.entrySet()) {
            String userName = entry.getKey();
            String ipInMap = entry.getValue();
            boolean found = false;

            for(String ipInArray : peerIps) {
                if(ipInMap.equals(ipInArray)) {
                    found = true;
                    break;
                }
            }

            if(!found)
                userIps.remove(userName);
        }
    }
}
