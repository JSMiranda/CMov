package pt.ulisboa.tecnico.cmov.cmovproject.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDeskFile;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Workspace;


public class ShowFilesInWorkspaceActivity extends ActionBarActivity {
    private Workspace workspace;
    private Collection<AirDeskFile> AirDeskFiles;
    private ArrayAdapter<String> fileAdapter;
    private ArrayList<String> fileNames;
    private String workspaceName;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);

        Intent intent = getIntent();
        workspaceName = intent.getStringExtra("WorkspaceName");
        AirDesk airDesk = AirDesk.getInstance(this);
        User user = airDesk.getMainUser();
        workspace = user.getOwnedWorkspaceByName(workspaceName);
        isOwner = intent.getBooleanExtra("isOwner", false);
        AirDeskFiles = workspace.getAirDeskFiles();

        setTitle(workspaceName);

        fileNames = new ArrayList<String>();
        for (AirDeskFile airDeskFile : AirDeskFiles) {
            fileNames.add(airDeskFile.getName());
        }
        fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileNames);
        final ListView filesList = (ListView) findViewById(R.id.filesList);
        filesList.setAdapter(fileAdapter);
        registerForContextMenu(filesList);
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                openFile(position, false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isOwner) {
            getMenuInflater().inflate(R.menu.menu_workspace, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:
                shareWorkspace();
                return true;
            case R.id.action_delete:
                AirDesk.getInstance(this).getMainUser().deleteWorkspace(workspace);
                exitActivity(item.getActionView());
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(ShowFilesInWorkspaceActivity.this, CreateWorkspaceActivity.class);
                intent.putExtra("wsName", workspaceName);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_file_long_press, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_file:
                deleteFileFromWorkspace(info.position);
                return true;
            case R.id.rename_file:
                renameFile(info.position);
                return true;
            case R.id.edit_file:
                openFile(info.position, true);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void openFile(int position, Boolean enabled) {
        Intent intent = new Intent(ShowFilesInWorkspaceActivity.this, EditFileActivity.class);
        intent.putExtra("fileName", fileAdapter.getItem(position));
        intent.putExtra("workspaceName", workspaceName);
        intent.putExtra("enabled", enabled.toString());
        startActivity(intent);
    }

    private void shareWorkspace() {
        Intent intent = new Intent(ShowFilesInWorkspaceActivity.this, WorkspacePermissionsActivity.class);
        intent.putExtra("workspaceName", workspaceName);
        intent.putExtra("parent", "WorkspaceActivity");
        startActivity(intent);
    }

    private void renameFile(int position) {
        final String fileName = fileAdapter.getItem(position);
        Intent intent = new Intent(ShowFilesInWorkspaceActivity.this, RenameFileActivity.class);
        intent.putExtra("OldName", fileName);
        intent.putExtra("WorkspaceName", workspaceName);
        startActivity(intent);
    }

    private void deleteFileFromWorkspace(int position) {
        final String fileName = fileAdapter.getItem(position);
        fileAdapter.remove(fileName);
        workspace.removeFile(fileName);
    }

    public void exitActivity(View v) {
        finish();
    }

    public void createFile(View v) {
        Intent intent = new Intent(ShowFilesInWorkspaceActivity.this, CreateFileActivity.class);
        intent.putExtra("WorkspaceName", workspaceName);
        startActivity(intent);
        finish();
    }
}
