package pt.ulisboa.tecnico.cmov.cmovproject.app;

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
import java.util.Arrays;
import java.util.Collection;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.File;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;


public class WorkspacePermissionsActivity extends ActionBarActivity {
    private WorkSpace workspace;
    private String workspaceName;
    //private ArrayList<String> checkedUsers;
    //private ArrayList<String> uncheckedUsers;
    ArrayAdapter<String> checkUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_permissions);

        Intent intent = getIntent();
        workspaceName = intent.getStringExtra("workspaceName");
        setTitle(workspaceName);
        AirDesk airDesk = AirDesk.getInstance("sarah_w@tecnico.ulisboa.pt", this);
        User user = airDesk.getMainUser();
        workspace = user.getOwnedWorkspaceByName(workspaceName);
        Collection<User> users;
        //users = workspace.getPermittedUsers(workspaceName)
        users = workspace.getPermittedUsers();
        ArrayList<String> checkedUsers = new ArrayList<String>();
        for(User iUser : users)
            checkedUsers.add(iUser.getNickname());
        //users = workspace.getUnPermittedUsers(workspaceName)
        ArrayList<String> uncheckedUsers = new ArrayList<String>(Arrays.asList("John", "Peter", "Bob"));
        //for(User iUser : users)
        //uncheckedUsers.add(iUser.getNickname());
        fillAmazingList(checkedUsers, uncheckedUsers);
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
        //space.savePermissions(workspaceName, getListCheckedUsers());
        Toast.makeText(WorkspacePermissionsActivity.this, "Changes saved!",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(WorkspacePermissionsActivity.this, WorkspaceActivity.class);
        intent.putExtra("WorkspaceName", workspaceName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void cancelShare(View v) {
        Toast.makeText(WorkspacePermissionsActivity.this, "Changes discarded!",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(WorkspacePermissionsActivity.this, WorkspaceActivity.class);
        intent.putExtra("WorkspaceName", workspaceName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void fillAmazingList(ArrayList<String> chckdUsers, ArrayList<String> unchckdUsers){
        chckdUsers = new ArrayList<String>(Arrays.asList("Joao", "Jose", "Edson")); //populated with example
        ArrayList<String> tempList = new ArrayList<String>();
        tempList.addAll(chckdUsers);
        tempList.addAll(unchckdUsers);
        ArrayAdapter<String> usersAdapter;
        checkUsersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, tempList);
        ListView amazingList = (ListView) findViewById(R.id.amazingList);
        amazingList.setAdapter(checkUsersAdapter);
        for(int i = chckdUsers.size()-1; i >= 0; i--){
            amazingList.setItemChecked(i,true);
        }
    }

    private ArrayList<String> getListCheckedUsers(){
        ArrayList<String> tempList = new ArrayList<String>();
        ListView amazingList = (ListView) findViewById(R.id.amazingList);
        SparseBooleanArray checked = amazingList.getCheckedItemPositions();
        int listSize = checkUsersAdapter.getCount();
        for (int i = 0; i < listSize; i++) {
            if (checked.get(i)) {
                tempList.add(checkUsersAdapter.getItem(i).toString());
            }
        }
        return tempList;
    }
}
