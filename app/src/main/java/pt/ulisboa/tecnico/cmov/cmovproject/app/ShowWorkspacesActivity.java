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
import android.widget.GridView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;


public class ShowWorkspacesActivity extends ActionBarActivity {
    private enum Showing {
        OWNED, FOREIGN
    }

    private static Showing state;
    private WorkSpaceAdapter wsAdapter = null;
    private final OwnedWorkSpaceAdapter ownedWsAdapter = new OwnedWorkSpaceAdapter(this);
    private final ForeignWorkSpaceAdapter foreignWsAdapter = new ForeignWorkSpaceAdapter(this);
    private GridView gridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workspaces);

        gridview = (GridView) findViewById(R.id.gridview);
        registerForContextMenu(gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent(ShowWorkspacesActivity.this, WorkspaceActivity.class);
                intent.putExtra("WorkspaceName", wsAdapter.getItem(position).getName());
                startActivity(intent);
            }
        });

        showOwnedWorkSpaces(gridview);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(state == Showing.OWNED) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_workspace, menu);
        } //FIXME: add option to display info (which will be possible if showing FOREIGN
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(state == Showing.OWNED) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteWorkspace(info.position);
                    return true;
                case R.id.action_edit:
                    editWorkspace(info.position);
                    return true;
                case R.id.action_share:
                    // TODO: share workspace...
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } else {
            //FIXME: add option to display info (which will be possible if showing FOREIGN
            return true;
        }
    }

    private void deleteWorkspace(int position) {
        WorkSpace ws = wsAdapter.getItem(position);
        String wsName = ws.getName();
        AirDesk airDesk = AirDesk.getInstance(this);
        airDesk.getMainUser().deleteWorkspace(ws);
        wsAdapter.notifyDataSetChanged();
        Toast.makeText(this, wsName, Toast.LENGTH_SHORT).show();
    }

    private void editWorkspace(int position) {
        WorkSpace ws = wsAdapter.getItem(position);
        String wsName = ws.getName();
        Intent intent = new Intent(ShowWorkspacesActivity.this, CreateWorkspaceActivity.class);
        intent.putExtra("wsName", wsName);
        startActivity(intent);
    }

    public void showOwnedWorkSpaces(View v) {
        findViewById(R.id.newWorkspaceButton).setVisibility(View.VISIBLE);
        findViewById(R.id.showPublicWorkspacesButton).setVisibility(View.GONE);
        findViewById(R.id.ownedWSButton).setBackgroundColor(getResources().getColor(R.color.light_blue));
        findViewById(R.id.foreignWSButton).setBackgroundColor(getResources().getColor(R.color.button_material_light));
        state = Showing.OWNED;
        wsAdapter = ownedWsAdapter;
        gridview.setAdapter(wsAdapter);
        wsAdapter.notifyDataSetChanged();
    }

    public void showForeignWorkSpaces(View v) {
        findViewById(R.id.showPublicWorkspacesButton).setVisibility(View.VISIBLE);
        findViewById(R.id.newWorkspaceButton).setVisibility(View.GONE);
        findViewById(R.id.foreignWSButton).setBackgroundColor(getResources().getColor(R.color.light_blue));
        findViewById(R.id.ownedWSButton).setBackgroundColor(getResources().getColor(R.color.button_material_light));
        state = Showing.FOREIGN;
        wsAdapter = foreignWsAdapter;
        gridview.setAdapter(wsAdapter);
        wsAdapter.notifyDataSetChanged();
    }

    public void startCreateWorkspaceActivity(View v) {
        Intent intent = new Intent(ShowWorkspacesActivity.this, CreateWorkspaceActivity.class);
        startActivity(intent);
    }

    public void startShowPublicWorkspacesActivity(View v) {
        Intent intent = new Intent(ShowWorkspacesActivity.this, ShowPublicWorkspacesActivity.class);
        startActivity(intent);
    }
}
