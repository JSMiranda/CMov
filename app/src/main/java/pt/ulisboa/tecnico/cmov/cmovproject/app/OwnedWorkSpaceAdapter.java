package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Context;

import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;

public class OwnedWorkSpaceAdapter extends WorkSpaceAdapter {
    public OwnedWorkSpaceAdapter(Context c) {
        super(c);
    }

    @Override
    public int getCount() {
        User u = AirDesk.getInstance(mContext).getMainUser();
        return u.getOwnedWorkSpaces().size();
    }

    @Override
    public WorkSpace getItem(int position) {
        User u = AirDesk.getInstance(mContext).getMainUser();
        return (WorkSpace) u.getOwnedWorkSpaces().values().toArray()[position];
    }
}
