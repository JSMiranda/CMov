package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;

public class CreateWorkspaceActivity extends ActionBarActivity {

    private ArrayList<String> tags = new ArrayList<String>();
    private ArrayAdapter<String> tagListAdapter;
    private WorkSpace ws = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workspace);

        String wsName = getIntent().getStringExtra("wsName");
        ws = null;
        if (wsName != null) {
            setTitle("Edit Workspace");
            ws = AirDesk.getInstance(this).getMainUser().getOwnedWorkspaceByName(wsName);
        }

        if (ws != null) {
            final EditText nameBox = (EditText) findViewById(R.id.nameInputBox);
            nameBox.setText(ws.getName());
            final EditText quotaBox = (EditText) findViewById(R.id.quotaInputBox);
            quotaBox.setText(Integer.toString(ws.getQuota()));
            for (String tag : ws.getTags()) {
                tags.add(tag);
            }

            final CheckBox checkBox = (CheckBox) findViewById(R.id.publicCheckBox);
            checkBox.setChecked(ws.isPublic());
        }

        tagListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tags);

        final ListView tagsList = (ListView) findViewById(R.id.tagsList);
        tagsList.setAdapter(tagListAdapter);

        tagsList.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                tagListAdapter.remove(tagsList.getItemAtPosition(position).toString());
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_workspace, menu);
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

    public void addTag(View v) {
        TextView tagInputBox = (TextView) findViewById(R.id.tagInputBox);
        if (tagInputBox.length() > 0) {
            String tag = tagInputBox.getText().toString();
            tagListAdapter.add(tag);
        }
        tagInputBox.setText("");
    }

    public void createWorkspace(View v) {
        TextView nameInputBox = (TextView) findViewById(R.id.nameInputBox);
        TextView quotaInputBox = (TextView) findViewById(R.id.quotaInputBox);
        TextView tagInputBox = (TextView) findViewById(R.id.tagInputBox);
        CheckBox checkBox = (CheckBox) findViewById(R.id.publicCheckBox);

        String workspaceName = nameInputBox.getText().toString();
        boolean isPublic = checkBox.isChecked();
        int quota;

        try {
            quota = Integer.parseInt(quotaInputBox.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Invalid quota!", Toast.LENGTH_LONG).show();
            quotaInputBox.setText("");
            return;
        }

        AirDesk airDesk = AirDesk.getInstance(this);
        User user = airDesk.getMainUser();
        if(ws == null) {
            user.createWorkspace(workspaceName, quota, isPublic); //we need to catch some exceptions were (duplicate workspaces .. etc.) TODO
        } else {
            user.setWorkSpaceName(ws.getName(), workspaceName);
            user.setWorkSpaceQuota(workspaceName, quota);
            if(isPublic) {
                user.setWorkSpaceToPublic(workspaceName);
            } else {
                user.setWorkSpaceToPrivate(workspaceName);
            }

        }

        user.removeAllTagsFromWorkSpace(workspaceName);

        for (String tag : tags) {
            user.addTagToWorkSpace(workspaceName, tag);
        }

        exitActivity(v);

    }

    public void exitActivity(View v) {
        Intent intent = new Intent(CreateWorkspaceActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
