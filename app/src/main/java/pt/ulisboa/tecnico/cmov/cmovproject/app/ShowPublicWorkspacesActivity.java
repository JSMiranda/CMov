package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import pt.ulisboa.tecnico.cmov.cmovproject.R;


public class ShowPublicWorkspacesActivity extends ActionBarActivity {
    private WorkSpaceAdapter wsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workspaces);

        GridView gridview = (GridView) findViewById(R.id.gridview);

        wsAdapter = new PublicWorkSpaceAdapter(this);
        gridview.setAdapter(wsAdapter);

        registerForContextMenu(gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                // TODO: Change this to exit this activity and show what happened with a toast
                Intent intent = new Intent(ShowPublicWorkspacesActivity.this, WorkspaceActivity.class);
                intent.putExtra("WorkspaceName", wsAdapter.getItem(position).getName());
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_public_workspaces, menu);
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
}
