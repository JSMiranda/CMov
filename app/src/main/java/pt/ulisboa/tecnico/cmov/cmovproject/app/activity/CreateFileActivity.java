package pt.ulisboa.tecnico.cmov.cmovproject.app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.exception.FileAlreadyExistsException;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDeskFile;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;

public class CreateFileActivity extends ActionBarActivity {

    private User user;
    private String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_file);

        AirDesk airDesk = AirDesk.getInstance(this);
        Intent intent = getIntent();
        workspaceName = intent.getStringExtra("WorkspaceName");
        user = airDesk.getMainUser();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_file, menu);
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

    public void createFile(View v) {
        EditText fileNameView = (EditText) findViewById(R.id.fileNameInput);
        String fileName = fileNameView.getText().toString();
        AirDeskFile airDeskFile = new AirDeskFile(fileName, 0, false);
        try {
            user.addFileToWorkSpace(workspaceName, airDeskFile);
            exit(v);
        } catch (FileAlreadyExistsException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void exit(View v) {
        finish();
    }
}
