package pt.ulisboa.tecnico.cmov.cmovproject.connectivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<Activity, Void, Boolean> {
    private static Socket client;
    private static PrintWriter printwriter;
    private String message;
    Activity activity;

    @Override
    protected Boolean doInBackground(Activity[] params) {
        activity = params[0];

        String[] strings = {"ola", "adeus"};

        if (strings.length <= 0) {
            return false;
        }
        // connect to the server and send the message
        try {
            client = new Socket("194.210.230.115", 1337);
            //client = new Socket("192.168.43.122", 1337);
            printwriter = new PrintWriter(client.getOutputStream(), true);
            printwriter.write(strings[0]);
            printwriter.flush();
            Toast.makeText(activity, strings[0], Toast.LENGTH_SHORT).show();
            printwriter.close();
            client.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}
