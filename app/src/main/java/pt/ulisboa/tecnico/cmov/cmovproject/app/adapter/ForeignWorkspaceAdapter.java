package pt.ulisboa.tecnico.cmov.cmovproject.app.adapter;

import android.content.Context;

import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.OwnedWorkspace;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;

public class ForeignWorkspaceAdapter extends WorkspaceAdapter {
    public ForeignWorkspaceAdapter(Context c) {
        super(c);
    }

    @Override
    public int getCount() {
        User u = AirDesk.getInstance(mContext).getMainUser();
        return u.getForeignWorkSpaces().size();
    }

    @Override
    public OwnedWorkspace getItem(int position) {
        User u = AirDesk.getInstance(mContext).getMainUser();
        return (OwnedWorkspace) u.getForeignWorkSpaces().values().toArray()[position];
    }
}
