package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class User {
    private Map<String, Workspace> ownedWorkspaces;
    private String nickname;
    private String email;

    public User(Map<String, Workspace> ownedWorkspaces, String nickname, String email) {
        this.ownedWorkspaces = new HashMap<String, Workspace>();
        this.nickname = nickname;
        this.email = email;
    }

    public void createWorkspace(String name, int quota, Collection<String> tags, boolean isPublic) {
        ownedWorkspaces.put(name, new Workspace(name, quota, tags, isPublic, this));
    }

    public void deleteWorkspace(Workspace ws) {
        ownedWorkspaces.remove(ws);
    }

    public void setWorkSpaceQuota(String workSpaceName, int quota) {
        Workspace ws = getWorkspaceByName(workSpaceName);
        ws.setQuota(quota);
    }

    void addTagToWorkSpace(String workSpaceName, Map<String, String> tags) {
        //TODO: Implement
    }

    void removeTagFromWorkSpace(String workSpaceName, Map<String, String> tags) {
        //TODO: Implement
    }

    public void setWorkSpaceName(String oldName, String newName) {
        Workspace ws = getWorkspaceByName(oldName);
        ws.setName(newName);
    }

    public void addUserToWorkSpace(String workSpaceName, User u) {
        Workspace ws = getWorkspaceByName(workSpaceName);
        ws.addPermittedUser(u);
    }

    public void removeUserFromWorkSpace(String workSpaceName, User u) {
        Workspace ws = getWorkspaceByName(workSpaceName);
        ws.removePermittedUser(u);
    }

    /*
     * Getters
     */

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    /*


    void addFile(File f) {
        files.add(f);
    }

    void removeFile(File f) {
        files.remove(f);
    }

    void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public User getOwner() {
        return owner;
    }

    void setName(String name) {
        this.name = name;
    }

    void addPermittedUser(User u) {
        permittedUsers.add(u);
    }

    void removePermittedUser(User u) {
        permittedUsers.remove(u);
    }

     */

    private Workspace getWorkspaceByName(String workSpaceName) {
        return ownedWorkspaces.get(workSpaceName);
    }
}
