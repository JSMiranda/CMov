package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;

public class WorkSpaceAdapter extends BaseAdapter {
    private Context mContext;

    public WorkSpaceAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        User u = AirDesk.getInstance(mContext).getMainUser();
        int count = 0;
        if (MainActivity.state == Showing.OWNED) {
            count = u.getOwnedWorkSpaces().size();
        } else if (MainActivity.state == Showing.FOREIGN) {
            count = u.getSubscribedWorkSpaces().size();
        }
        return count;
    }

    @Override
    public WorkSpace getItem(int position) {
        User u = AirDesk.getInstance(mContext).getMainUser();
        WorkSpace ws = null;
        if (MainActivity.state == Showing.OWNED) {
            ws = (WorkSpace) u.getOwnedWorkSpaces().values().toArray()[position];
        } else if (MainActivity.state == Showing.FOREIGN) {
            ws = (WorkSpace) u.getSubscribedWorkSpaces().values().toArray()[position];
        }
        return ws;
    }

    @Override
    // Method not used
    public long getItemId(int position) {
        return 0;
    }

    @Override
    // create a new ImageView for each item referenced by the Adapter
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

    // references to our images FIXME: public/private folder images
    private Integer[] mThumbIds = {
            R.drawable.ic_action_discard, R.drawable.ic_action_discard,
    };
}