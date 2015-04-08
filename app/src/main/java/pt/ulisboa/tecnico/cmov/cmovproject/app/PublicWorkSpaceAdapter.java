package pt.ulisboa.tecnico.cmov.cmovproject.app;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.cmovproject.model.AirDesk;
import pt.ulisboa.tecnico.cmov.cmovproject.model.User;
import pt.ulisboa.tecnico.cmov.cmovproject.model.WorkSpace;

public class PublicWorkSpaceAdapter extends WorkSpaceAdapter {
    public PublicWorkSpaceAdapter(Context c) {
        super(c);
    }

    @Override
    public int getCount() {
        List<User> users = new ArrayList<User>();
        users.addAll(AirDesk.getInstance(mContext).getOtherUsers()); // FIXME: This should not be needed. Getters should return copies.
        users.add(AirDesk.getInstance(mContext).getMainUser()); // TODO: In the 2nd part of the project, remove this line
        int count = 0;
        for(User u : users) {
            for(WorkSpace ws : u.getOwnedWorkSpaces().values()) {
                if(ws.isPublic()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public WorkSpace getItem(int position) {
        // FIXME: should be O(1)...
        List<User> users = new ArrayList<User>();
        users.addAll(AirDesk.getInstance(mContext).getOtherUsers()); // FIXME: This should not be needed. Getters should return copies.
        users.add(AirDesk.getInstance(mContext).getMainUser()); // TODO: In the 2nd part of the project, remove this lines
        int count = 0;
        for(User u : users) {
            for(WorkSpace ws : u.getOwnedWorkSpaces().values()) {
                if(ws.isPublic()) {
                    if(count == position) {
                        return ws;
                    }
                    count++;
                }
            }
        }

        throw new IllegalStateException("Calling getItem when size is 0");
    }
}
