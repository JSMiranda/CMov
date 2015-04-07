package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;


public class WorkspacePermissionsActivity extends ActionBarActivity {
    private WorkSpace workspace;
    private String workspaceName;

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
        //space.savePermissions(workspaceName, userName);
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
}
