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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;

public class ConnectivityService extends Service implements GroupInfoListener {
    boolean mWiFiDirectIsOn = false;
    SimWifiP2pManager mManager = null;
    Channel mChannel = null;

    private String mEmail = null;
    private TreeMap<String, String> userIps = new TreeMap<String, String>();

    private SimWifiP2pBroadcastReceiver mReceiver;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private ConnectivityThread thread;

    private final String TAG = this.getClass().getName();

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast mReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        mEmail = intent.getStringExtra("Email");

        if (!mWiFiDirectIsOn) {
            toggleWiFiDirect();
        }

        thread = new ConnectivityThread();
        thread.start();

        new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        AirDesk.getInstance(getApplicationContext()).setConnService(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public boolean toggleWiFiDirect() {
        if (mWiFiDirectIsOn) {
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
                Log.d(TAG, "Server blocked, waiting for request");
                st = sockIn.readLine();
                Log.d(TAG, "Server unblocked, read " + st);
                JSONObject request = null;
                try {
                    request = new JSONObject(st);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // defensive removal of newlines. Remove in the end or... just in case of bad performance?
                String response = computeResponse(request).replaceAll("[\n]", " ") + "\n";
                s.getOutputStream().write(response.getBytes());
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            }
            return st;
        }

        private String computeResponse(JSONObject request) {
            try {
                switch (request.getString("RequestType")) {
                    case "tellEmail":
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("Email", mEmail);
                        jsonObj.put("Nick", AirDesk.getInstance().getMainUser().getNickname());
                        request.put("Response", jsonObj);
                        return request.toString();
                    case "subscribeWorkspace":
                        break;
                    case "unsubscribeWorkspace":
                        break;
                    case "shareWorkspace":
                        String ownerEmail = request.getString("ownerEmail");
                        String workspaceName = request.getString("workspaceName");
                        JSONArray jArr = request.getJSONArray("fileNames");
                        ArrayList<String> fileNames = new ArrayList<>();
                        for (int i = 0 ; i < jArr.length() ; i++) {
                            fileNames.add(jArr.getString(i));
                        }
                        AirDesk.getInstance().getMainUser().addForeignWorkspace(workspaceName, ownerEmail, fileNames);
                        return request.toString();
                    case "unshareWorkspace":
                        break;
                    case "fetchFile":
                        break;
                    case "requestLock":
                        break;
                    case "editFile":
                        break;
                    case "notifyNewFile":
                        break;
                    case "notifyFileEdited":
                        break;
                    case "notifyFileDeleted":
                        break;
                    case "notifyWorkspaceEdited":
                        break;
                    case "notifyWorkspaceDeleted":
                        break;
                    default:
                        throw new UnsupportedOperationException(request.getString("RequestType"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        ArrayList<String> peerIps = new ArrayList<String>();
        int i = 0;
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devStr = "" + deviceName + " (" +
                    ((device == null) ? "??" : device.getVirtIp()) + ")\n";
            peersStr.append(devStr);

            if (device != null)
                peerIps.add(device.getVirtIp());
        }

        Log.d(TAG, peersStr.toString());
        removeExitingPeersFromUserMap(peerIps);
        addJoiningPeersToUserMap(peerIps);
    }

    private synchronized void removeExitingPeersFromUserMap(ArrayList<String> peerIps) {
        ArrayList<String> keysToRemove = new ArrayList<String>();

        for (Map.Entry<String, String> entry : userIps.entrySet()) {
            String userName = entry.getKey();
            String ipInMap = entry.getValue();
            boolean found = false;

            for (String ipInArray : peerIps) {
                if (ipInMap.equals(ipInArray)) {
                    found = true;
                    break;
                }
            }

            if (!found)
                keysToRemove.add(userName);
        }

        for (String key : keysToRemove) {
            userIps.remove(key);
        }
    }

    private void addJoiningPeersToUserMap(final ArrayList<String> peerIps) {
        ArrayList<String> newIps = new ArrayList<String>();

        synchronized (this) {
            for (String ip : peerIps) {
                if (!userIps.containsValue(ip)) {
                    newIps.add(ip);
                }
            }
        }

        for (String ip : newIps) {
            askForUsername(ip);
        }
    }

    private void askForUsername(final String ip) {
        final JSONObject message = new JSONObject();
        try {
            message.put("RequestType", "tellEmail");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendMessage(ip, message, new ResponseHandler() {
            @Override
            public void run() {
                String email = null;
                try {
                    email = getResponse().getJSONObject("Response").getString("Email");
                    String nick = getResponse().getJSONObject("Response").getString("Nick");
                    AirDesk.getInstance().addUser(nick, email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                synchronized (ConnectivityService.this) {
                    userIps.put(email, ip);
                    Log.d(TAG, "Map changed: " + userIps);
                }
            }
        });
    }

    public void shareWorkspace(final String userEmail, final String workspaceName, final ArrayList<String> fileNames) {
        final JSONObject message = new JSONObject();
        try {
            message.put("RequestType", "shareWorkspace");
            message.put("ownerEmail", mEmail);
            message.put("workspaceName", workspaceName);
            JSONArray jArr = new JSONArray(fileNames);
            message.put("fileNames", jArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String ip;
        synchronized (this) {
            ip = userIps.get(userEmail);
        }

        sendMessage(ip, message, new ResponseHandler() {
            @Override
            public void run() {
                User user = AirDesk.getInstance().getOtherUserByEmail(userEmail);
                AirDesk.getInstance().getMainUser().addUserToWorkSpace(workspaceName, user);
            }
        });
    }

    private void sendMessage(final String ip, JSONObject message, final ResponseHandler responseHandler) {
        final String messageToSend = message.toString();
        thread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(ip,
                            Integer.parseInt(getString(R.string.port)));
                    // defensive removal of newlines
                    mCliSocket.getOutputStream().write((messageToSend.replaceAll("[\n]", " ") + "\n").getBytes());
                    BufferedReader sockIn = new BufferedReader(new InputStreamReader(mCliSocket.getInputStream()));
                    Log.d(TAG, "Client blocked, waiting for response. Server has ip " + ip);
                    String response = sockIn.readLine();
                    Log.d(TAG, "Client unblocked, has read " + response);
                    mCliSocket.close(); // TODO: check null answer
                    JSONObject jsonResponse = new JSONObject(response);
                    responseHandler.setResponse(jsonResponse);
                    responseHandler.run();
                } catch (IOException | JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
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

    private abstract class ResponseHandler implements Runnable {
        private JSONObject response;

        public void setResponse(JSONObject response) {
            this.response = response;
        }

        public JSONObject getResponse() {
            return response;
        }
    }
}
