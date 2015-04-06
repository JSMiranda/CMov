package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;


public class EditFileActivity extends ActionBarActivity {

    private WorkSpace workspace;
    private String fileName;
    private String workSpaceName;
    private ArrayList<String> tags = new ArrayList<String>();
    private ArrayAdapter<String> tagListAdapter;
    EditText fileEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("FileName");
        workSpaceName = intent.getStringExtra("WorkspaceName");
        setTitle(workSpaceName + "/" + fileName);
        fileEditText = (EditText) findViewById(R.id.fileEditText);
        String fileText = "... File text...";
        //String fileText = workspace.getFileText(workSpaceName, fileName);
        fileEditText.setText(fileText);
        AirDesk airDesk = AirDesk.getInstance("sarah_w@tecnico.ulisboa.pt", this);
        User user = airDesk.getMainUser();
        workspace = user.getOwnedWorkspaceByName(workSpaceName);
        tagListAdapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tags);

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addTag(View v) {
        TextView tagInputBox = (TextView) findViewById(R.id.tagInputBox);
        String tag = tagInputBox.getText().toString();
        tagListAdapter.add(tag);
        tagInputBox.setText("");
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
}
