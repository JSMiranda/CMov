package pt.ulisboa.tecnico.cmov.cmovproject.connectivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
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
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.cmovproject.R;

public class ConnectivityService extends Service implements GroupInfoListener {
    boolean mWiFiDirectIsOn = false;
    SimWifiP2pManager mManager = null;
    Channel mChannel = null;

    private TreeMap<String, String> userIps = new TreeMap<String, String>();
    private SimWifiP2pSocketServer mSrvSocket = null;
    private ConnectivityThread thread;

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

        thread = new ConnectivityThread();
        thread.start();

        new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

    public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt(getString(R.string.port)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    publishProgress(sock);
                } catch (IOException e) {
                    Log.d("Error accepting socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket... values) {
            SimWifiP2pSocket sock = values[0];

            ReceiveCommTask comm = new ReceiveCommTask();
            comm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sock);
        }
    }

    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, String> {
        SimWifiP2pSocket s;

        @Override
        protected String doInBackground(SimWifiP2pSocket... params) {
            BufferedReader sockIn;
            String st = null;

            s = params[0];
            try {
                sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
                Log.d(TAG, "Server will read");
                st = sockIn.readLine();
                Log.d(TAG, "Server unlocked, read " + st);
                s.getOutputStream().write("xD\n".getBytes());
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            }
            return st;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!s.isClosed()) {
                try {
                    s.close();
                }
                catch (Exception e) {
                    Log.d("Error closing socket:", e.getMessage());
                }
            }

            s = null;

            try {
                JSONObject message = new JSONObject(result);
            } catch (JSONException e) {
                Log.d(TAG, "Error while parsing JSON object");
            }
        }
    }

    private void addJoiningPeersToUserMap(final ArrayList<String> peerIps) {
        for(String ip : peerIps) {
            if(!userIps.containsValue(ip)) {
                askForUsername(ip);
            }
        }
    }

    private void askForUsername(final String ip) {
        final JSONObject message = new JSONObject();
        try {
            message.put("RequestType", "tellEmail");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendMessage(ip, message, new JSONObject[1]);
    }

    private void sendMessage(final String ip, JSONObject message, final JSONObject[] response) {
        final String messageToSend = message.toString();
        thread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(ip,
                            Integer.parseInt(getString(R.string.port)));
                    mCliSocket.getOutputStream().write((messageToSend.replaceAll("[\n]", " ") + "\n").getBytes());
                    BufferedReader sockIn = new BufferedReader(new InputStreamReader(mCliSocket.getInputStream()));
                    Log.d(TAG, "Client will read, server has ip " + ip);
                    String st = sockIn.readLine();
                    Log.d(TAG, "Client unlocked, read " + st);
                    mCliSocket.close();
                    response[0] = new JSONObject(st);
                    // TODO: Change the way we react to responses
                } catch (UnknownHostException e) {
                    Log.d(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
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

    private class ConnectivityThread extends Thread {
        private Handler handler;

        @Override
        public void run() {
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        }

        public Handler getHandler() {
            return handler;
        }
    }

}
