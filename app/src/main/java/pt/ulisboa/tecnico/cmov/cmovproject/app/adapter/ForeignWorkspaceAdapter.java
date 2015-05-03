package pt.ulisboa.tecnico.cmov.cmovproject.app.adapter;

import android.content.Context;

import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Workspace;

public class ForeignWorkspaceAdapter extends WorkspaceAdapter {
    public ForeignWorkspaceAdapter(Context c) {
        super(c);
    }

    @Override
    public int getCount() {
        User u = AirDesk.getInstance(mContext).getMainUser();
        return u.getSubscribedWorkSpaces().size();
    }

    @Override
    public Workspace getItem(int position) {
        User u = AirDesk.getInstance(mContext).getMainUser();
        return (Workspace) u.getSubscribedWorkSpaces().values().toArray()[position];
    }
}
