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

public class EditFileActivity extends ActionBarActivity {

    private OwnedWorkspace workspace;
    private String fileName;
    private String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        AirDesk airDesk = AirDesk.getInstance(this);
        User user = airDesk.getMainUser();

        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        workspaceName = intent.getStringExtra("workspaceName");
        setTitle(workspaceName + "/" + fileName);
        EditText fileEditText = (EditText) findViewById(R.id.fileEditText);
        workspace = user.getOwnedWorkspaceByName(workspaceName);
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
        if(workspace.saveFile(fileName, text))
            backToParent("Changes saved!");
        else
            Toast.makeText(EditFileActivity.this, "Quota limit exceeded",
                    Toast.LENGTH_SHORT).show();
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
        EditText fileEditText = (EditText) findViewById(R.id.fileEditText);
        setTextEditable(!fileEditText.isFocusable());
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
