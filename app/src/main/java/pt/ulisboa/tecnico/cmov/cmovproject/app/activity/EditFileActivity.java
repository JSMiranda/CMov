package pt.ulisboa.tecnico.cmov.cmovproject.app.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Workspace;

public class EditFileActivity extends ActionBarActivity {

    private Workspace workspace;
    private String fileName;
    private String workspaceName;
    private boolean isOwned;
    private boolean isLocked = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        AirDesk airDesk = AirDesk.getInstance(this);
        User user = airDesk.getMainUser();

        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        workspaceName = intent.getStringExtra("workspaceName");
        isOwned = intent.getBooleanExtra("isOwned", true);
        setTitle(workspaceName + "/" + fileName);
        EditText fileEditText = (EditText) findViewById(R.id.fileEditText);
        if(isOwned) {
            workspace = user.getOwnedWorkspaceByName(workspaceName);
        } else {
            workspace = user.getForeignWorkspaceByName(workspaceName);
        }

        String fileText = workspace.openFileByName(fileName);
        boolean enabled = Boolean.parseBoolean(intent.getStringExtra("enabled"));
        fileEditText.setText(fileText);
        setTextEditable(enabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_delete:
                deleteFileFromWorkspace();
                return true;
            case R.id.action_edit:
                toggleEditable();
                return true;
            case R.id.action_search:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void saveFile(View v) {
        String text = ((EditText) findViewById(R.id.fileEditText)).getText().toString();
        if(!isOwned && AirDesk.getInstance().getMainUser().getForeignWorkspaceByName(workspaceName) == null) {
            Toast.makeText(EditFileActivity.this, "Cannot save. Workspace name changed.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(workspace.getFile(fileName) == null) {
            Toast.makeText(EditFileActivity.this, "Cannot save. File name changed.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(workspace.saveFile(fileName, text)) {
            backToParent("Changes saved!");
            workspace.unlock(fileName, AirDesk.getInstance().getMainUser().getEmail(), workspaceName);
            isLocked = false;
        }
        else {
            Toast.makeText(EditFileActivity.this, "Quota limit exceeded",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelEdit(View v) {
        backToParent("Changes discarded!");
    }

    private void backToParent(String toast){
        Toast.makeText(EditFileActivity.this, toast,
                Toast.LENGTH_SHORT).show();
        finish();
    }
    public void toggleEditable(){
        AirDesk airDesk = AirDesk.getInstance();
        if(!isLocked) {
            if(workspace.tryLock(fileName, airDesk.getMainUser().getEmail(), workspaceName)) {
                isLocked = true;
            } else {
                Toast.makeText(EditFileActivity.this, "This file is already locked.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            workspace.unlock(fileName, airDesk.getMainUser().getEmail(), workspaceName);
            isLocked = false;
        }
        setTextEditable(isLocked);


    }

    public void setTextEditable(boolean editable){
        EditText fileEditText = (EditText) findViewById(R.id.fileEditText);
        fileEditText.setFocusable(editable);
        fileEditText.setClickable(editable);
        fileEditText.setFocusableInTouchMode(editable);
        fileEditText.setLongClickable(editable);
        fileEditText.setTextColor(editable? Color.BLACK : Color.DKGRAY);
        InputMethodManager inputMM = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editable) {
            fileEditText.requestFocus();
            inputMM.showSoftInput(fileEditText, 0);
        }
        else
            inputMM.hideSoftInputFromWindow(fileEditText.getWindowToken(), 0);
    }


    private void deleteFileFromWorkspace() {
        workspace.removeFile(fileName);
        backToParent("File deleted!");
    }
}
