package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;


public class LoginActivity extends ActionBarActivity {
    private class LoadTask extends AsyncTask {
        @Override
        protected Boolean doInBackground(Object[] params) {
            AirDesk airDesk = AirDesk.getInstance(LoginActivity.this);
            return airDesk.load();
        }

        @Override
        protected void onPostExecute(Object o) {
            if((Boolean)o) {
                startShowWorkspaceActivity();
            } else {
                setContentView(R.layout.activity_login);
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LoadTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View v) {
        TextView emailView = (TextView) findViewById(R.id.email);
        TextView nickNameView = (TextView) findViewById(R.id.nickname);
        String email = emailView.getText().toString();
        String nickname = nickNameView.getText().toString();

        if (!isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Invalid email address!", Toast.LENGTH_LONG).show();
            return;
        }

        AirDesk airDesk = AirDesk.getInstance(this);

        airDesk.init(email, nickname);
        startShowWorkspaceActivity();
    }

    private void startShowWorkspaceActivity() {
        Intent intent = new Intent(LoginActivity.this, ShowWorkspacesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
