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
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AirDesk airDesk = AirDesk.getInstance("sarah_w@tecnico.ulisboa.pt", this);
//        User user = airDesk.getMainUser();
//        ArrayList<String> workspaceNames = user.getOwnedWorkspaceNames();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        final WorkSpaceAdapter wsAdapter = new WorkSpaceAdapter(this);
        gridview.setAdapter(wsAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent intent = new Intent(MainActivity.this, WorkspaceActivity.class);
                intent.putExtra("WorkspaceName", wsAdapter.getItem(position).getName());
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "" + position,
//                        Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        //////// REMOVED
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    public void startCreateWorkspaceActivity(View v) {
        Intent intent = new Intent(MainActivity.this, CreateWorkspaceActivity.class);
        startActivity(intent);
    }
}
