package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;
import pt.ulisboa.tecnico.cmov.cmovproject.model.airDeskFile;

public class EditFileActivity extends ActionBarActivity {

    private WorkSpace workspace;
    private String fileName;
    private String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        workspaceName = intent.getStringExtra("workspaceName");
        setTitle(workspaceName + "/" + fileName);
        EditText fileEditText = (EditText) findViewById(R.id.fileEditText);
        //String fileText = workspace.getFileText(workspaceName, fileName);
        String fileText = airDeskFile.readFromFile(fileName+".txt");
        boolean enabled = Boolean.parseBoolean(intent.getStringExtra("enabled"));
        fileEditText.setText(fileText);
        setTextEditable(enabled);
        AirDesk airDesk = AirDesk.getInstance(this);
        User user = airDesk.getMainUser();
        workspace = user.getOwnedWorkspaceByName(workspaceName);
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
        //space.saveFile(workspaceName, fileName, fileEditText.getText().toString());
        airDeskFile.writeToFile(fileName+".txt", "" + ((EditText) findViewById(R.id.fileEditText)).getText().toString());
        backToParent("Changes saved!");
    }

    public void cancelEdit(View v) {
        backToParent("Changes discarded!");
    }

    private void backToParent(String toast){
        Toast.makeText(EditFileActivity.this, toast,
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditFileActivity.this, WorkspaceActivity.class);
        intent.putExtra("WorkspaceName", workspaceName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
    }

}
