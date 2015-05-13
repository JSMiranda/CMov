package pt.ulisboa.tecnico.cmov.cmovproject.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Workspace;

/**
 * This adapter allows drawing of multiple elements to represent a workspace
 * (currently one image and a text box that shows its name). The class is abstract
 * because items are retrieved in multiple ways, each depending on the particular
 * state of observation.
 * @see OwnedWorkspaceAdapter
 * @see ForeignWorkspaceAdapter
 * @see PublicWorkspaceAdapter
 */
public abstract class WorkspaceAdapter extends BaseAdapter {
    Context mContext;

    public WorkspaceAdapter(Context c) {
        mContext = c;
    }

    public abstract Workspace getItem(int position);

    @Override
    // Method not used
    public long getItemId(int position) {
        return 0;
    }

    @Override
    // create a new WorkspaceView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ImageView imageView;
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.workspace_view, null);
        } else {
            view = convertView;
        }
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(mThumbIds[0]);
        textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(getItem(position).getName());

        return view;
    }

    // TODO: public/private folder images (only first position is being used at the moment
    private Integer[] mThumbIds = {
            R.drawable.folder, R.drawable.ic_action_discard,
    };
}