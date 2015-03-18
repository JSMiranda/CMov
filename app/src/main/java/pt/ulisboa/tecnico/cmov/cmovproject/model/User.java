package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.ArrayList;
import java.util.Collection;

public class User {
    private Collection<Workspace> ownedWorkspaces;
    private String nickname;
    private String email;

    public User() {
        ownedWorkspaces = new ArrayList<Workspace>();
    }

    public void createWorkspace() {
        ownedWorkspaces.add(new Workspace());
    }
}
