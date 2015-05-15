package pt.ulisboa.tecnico.cmov.cmovproject.app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.exception.FileAlreadyExistsException;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;

public class RenameFileActivity extends ActionBarActivity {

    private OwnedWorkspace workspace;
    private TextView inputBox;
    private String oldName;
    private String workspaceName;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_file);

        Intent intent = getIntent();
        oldName = intent.getStringExtra("OldName");
        setTitle(getTitle() + " (" + oldName + ")");
        workspaceName = intent.getStringExtra("WorkspaceName");
        isOwner = intent.getBooleanExtra("isOwner", false);

        inputBox = (TextView) findViewById(R.id.newNameBox);
        inputBox.setText(oldName);

        AirDesk airDesk = AirDesk.getInstance(this);
        User user = airDesk.getMainUser();
        workspace = user.getOwnedWorkspaceByName(workspaceName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rename_file, menu);
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

    public void renameFile(View v) {
        String newName = inputBox.getText().toString();
        if(!workspace.getFile(oldName).isLocked()) {
            try {
                workspace.renameFile(oldName, newName);
                for (User u : AirDesk.getInstance().getMainUser().getOwnedWorkspaceByName(workspaceName).getPermittedUsers()) {
                    AirDesk.getInstance().getConnService().notifyFileRenamed(u.getEmail(), workspaceName, oldName, newName);
                }
            } catch (FileAlreadyExistsException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "The file is locked, it cannot be renamed!", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(RenameFileActivity.this, ShowFilesInWorkspaceActivity.class);
        intent.putExtra("WorkspaceName", workspaceName);
        intent.putExtra("isOwner", isOwner);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void exitActivity(View v) {
        finish();
    }
}
