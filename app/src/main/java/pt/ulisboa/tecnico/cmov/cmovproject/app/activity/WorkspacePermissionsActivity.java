package pt.ulisboa.tecnico.cmov.cmovproject.app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.OwnedWorkspace;


public class WorkspacePermissionsActivity extends ActionBarActivity {
    private OwnedWorkspace workspace;
    private String workspaceName;
    ArrayAdapter<String> checkUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_permissions);

        Intent intent = getIntent();
        workspaceName = intent.getStringExtra("workspaceName");
        setTitle(workspaceName);
        AirDesk airDesk = AirDesk.getInstance(this);
        User thisUser = airDesk.getMainUser();
        workspace = thisUser.getOwnedWorkspaceByName(workspaceName);
        Collection <User> listUsers = workspace.getPermittedUsers();
        int numCheckedUsers = listUsers.size();
        ArrayList<String> listUserNames = new ArrayList<String>();
        for(User iUser : listUsers)
            listUserNames.add(iUser.getNickname());
        listUsers = airDesk.getOtherUsers();
        for(User iUser : listUsers)
            if(!listUserNames.contains(iUser.getNickname()))
                listUserNames.add(iUser.getNickname());

        // TODO: Remove in the 2nd part of project
        if(!listUserNames.contains(thisUser.getNickname())) {
            listUserNames.add(numCheckedUsers, thisUser.getNickname());
        }

        fillAmazingList(listUserNames, numCheckedUsers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workspace_permissions, menu);
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

    public void saveShare(View v) {
        saveListUsers();
        backToParent("Changes saved!");
    }

    public void cancelShare(View v) {
        backToParent("Changes discarded!");
    }

    private void backToParent(String toast){
        Toast.makeText(WorkspacePermissionsActivity.this, toast,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void fillAmazingList(ArrayList<String>listUserNames, int numCheckedUsers){
        checkUsersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, listUserNames);
        ListView amazingList = (ListView) findViewById(R.id.amazingList);
        amazingList.setAdapter(checkUsersAdapter);
        for(int i = numCheckedUsers-1; i >= 0; i--){
            amazingList.setItemChecked(i,true);
        }
    }

    private void saveListUsers(){
        ListView amazingList = (ListView) findViewById(R.id.amazingList);
        SparseBooleanArray checked = amazingList.getCheckedItemPositions();

        AirDesk airDesk = AirDesk.getInstance(this);
        User thisUser = airDesk.getMainUser();
        ArrayList<User> listUsers = new ArrayList<User>(workspace.getPermittedUsers());
        listUsers.addAll(airDesk.getOtherUsers());
        if(!listUsers.contains(thisUser))
            listUsers.add(thisUser); // TODO: In the 2nd part of the project, remove this lines
        HashMap<String,User> mapUsers = new HashMap<String,User>();
        for(User iUser : listUsers)
            mapUsers.put(iUser.getNickname(), iUser);

        ArrayList<User> alreadyPermitted = new ArrayList<User>(workspace.getPermittedUsers());
        for (int i = 0; i < checkUsersAdapter.getCount(); i++) {
            if (!checked.get(i) && alreadyPermitted.contains(mapUsers.get(checkUsersAdapter.getItem(i)))) {
                thisUser.unshareWorkspace(workspaceName, mapUsers.get(checkUsersAdapter.getItem(i)));
            } else if (checked.get(i) && !alreadyPermitted.contains(mapUsers.get(checkUsersAdapter.getItem(i)))) {
                thisUser.shareWorkspace(workspaceName, mapUsers.get(checkUsersAdapter.getItem(i)));
            }
        }
    }
}
