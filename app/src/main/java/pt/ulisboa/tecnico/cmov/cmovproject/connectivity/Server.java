package pt.ulisboa.tecnico.cmov.cmovproject.connectivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends AsyncTask<Activity, Void, Boolean> {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private String message;
    Activity activity;

    @Override
    protected Boolean doInBackground(Activity[] params) {
        activity = params[0];

        try {
            serverSocket = new ServerSocket(1337);
        } catch (IOException e) {
            message = "Could not listen on port: 1337";
            Log.d("Server", message);
        }

        Log.d("Server", "Server started. Listening to the port 1337");

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                inputStreamReader =
                        new InputStreamReader(clientSocket.getInputStream());
                bufferedReader =
                        new BufferedReader(inputStreamReader);
                message = bufferedReader.readLine();

                Log.d("Server", message);

                inputStreamReader.close();
                clientSocket.close();

            } catch (IOException ex) {
                Log.d("Server", "Problem in message reading");
            }
        }

    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}

