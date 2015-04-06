package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.File;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;


public class WorkspaceActivity extends ActionBarActivity {

    private WorkSpace workSpace;

    private Collection<File> files;

    private ArrayAdapter<String> fileAdapter;

    private ArrayList<String> fileNames;

    private String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);

        Intent intent = getIntent();
        workspaceName = intent.getStringExtra("WorkspaceName");

        AirDesk airDesk = AirDesk.getInstance("MyName");
        User user = airDesk.getMainUser();
        workSpace = user.getWorkspaceByName(workspaceName);
        files = workSpace.getFiles();

        fileNames = new ArrayList<String>();

        for(File file : files) {
            fileNames.add(file.getName());
        }

        fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileNames);

        final ListView filesList = (ListView) findViewById(R.id.filesList);

        filesList.setAdapter(fileAdapter);

        registerForContextMenu(filesList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workspace, menu);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_workspace, menu);
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
                editFile(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void editFile(int position){
        Intent intent = new Intent(WorkspaceActivity.this, EditFileActivity.class);
        intent.putExtra("FileName", fileAdapter.getItem(position));
        intent.putExtra("WorkspaceName", workspaceName);
        startActivity(intent);
    }

    private void renameFile(int position) {
        final String fileName = fileAdapter.getItem(position);

        Intent intent = new Intent(WorkspaceActivity.this, RenameFileActivity.class);
        intent.putExtra("OldName", fileName);
        intent.putExtra("WorkspaceName", workspaceName);
        startActivity(intent);

        //fileAdapter.remove(fileName);
        //fileAdapter.insert("TEST", position);
        //workSpace.renameFile(fileName, "TEST");
    }

    private void deleteFileFromWorkspace(int position) {
        final String fileName = fileAdapter.getItem(position);
        fileAdapter.remove(fileName);
        workSpace.removeFileByName(fileName);
        Toast.makeText(WorkspaceActivity.this, fileName, Toast.LENGTH_SHORT).show();
    }

    public void exitActivity(View v) {
        Intent intent = new Intent(WorkspaceActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
