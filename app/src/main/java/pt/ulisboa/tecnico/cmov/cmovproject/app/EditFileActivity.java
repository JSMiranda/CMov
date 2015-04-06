package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputType;
import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;

public class EditFileActivity extends ActionBarActivity {

    private WorkSpace workspace;
    private String fileName;
    private String workSpaceName;
    private EditText fileEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        workSpaceName = intent.getStringExtra("workspaceName");
        setTitle(workSpaceName + "/" + fileName);
        fileEditText = (EditText) findViewById(R.id.fileEditText);
        String fileText = "... File text...";
        boolean enabled = Boolean.parseBoolean(intent.getStringExtra("enabled"));
        //String fileText = workspace.getFileText(workSpaceName, fileName);
        fileEditText.setText(fileText);
        fileEditText.setEnabled(enabled);
        AirDesk airDesk = AirDesk.getInstance("sarah_w@tecnico.ulisboa.pt", this);
        User user = airDesk.getMainUser();
        workspace = user.getOwnedWorkspaceByName(workSpaceName);
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
        switch (item.getItemId()) {
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
        //space.saveFile(workSpaceName, fileName, fileEditText.getText().toString());
        Toast.makeText(EditFileActivity.this, "Changes saved!",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditFileActivity.this, WorkspaceActivity.class);
        intent.putExtra("WorkspaceName", workSpaceName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void cancelEdit(View v) {
                Toast.makeText(EditFileActivity.this, "Changes discarded!",
                        Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditFileActivity.this, WorkspaceActivity.class);
        intent.putExtra("WorkspaceName", workSpaceName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void toggleEditable(){
        fileEditText.setEnabled(!fileEditText.isEnabled());
    }

}
