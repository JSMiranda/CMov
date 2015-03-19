package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.Collection;
import java.util.Map;

/**
 * A User has a set of workspaces (owned)
 * and is represented by their nickname and email.
 * A user is responsible for managing their own workspaces.
 */
public class User {
    private Map<String, WorkSpace> ownedWorkSpaces;
    private Map<String, WorkSpace> subscribedWorkSpaces;
    private String nickname;
    private String email;

    /**
     * Initializes this {@code User}. Can be used to create a new user or load an existing one
     *
     * @param ownedWorkSpaces      Use {@code null} if creating a new user
     * @param subscribedWorkSpaces Use {@code null} if creating a new user
     * @param nickname             User's nickname
     * @param email                User's email
     */
    public User(Map<String, WorkSpace> ownedWorkSpaces, Map<String, WorkSpace> subscribedWorkSpaces, String nickname, String email) {
        this.ownedWorkSpaces = ownedWorkSpaces;
        this.subscribedWorkSpaces = subscribedWorkSpaces;
        this.nickname = nickname;
        this.email = email;
    }

    public void createWorkspace(String name, int quota, Collection<String> tags, boolean isPublic) {
        ownedWorkSpaces.put(name, new WorkSpace(name, quota, tags, isPublic, this));
    }

    /**
     * Delete a given workspace. To get user's workspaces use {@link User#getOwnedWorkSpaces}
     *
     * @param ws instance of WorkSpace to delete.
     */
    public void deleteWorkspace(WorkSpace ws) {
        ownedWorkSpaces.remove(ws);
    }

    public void subscribeWorkspace(WorkSpace ws) {
        ws.addPermittedUser(this);
    }

    public void unsubscribeWorkspace(WorkSpace ws) {
        ws.removePermittedUser(this);
    }

    public void setWorkSpaceQuota(String workSpaceName, int quota) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.setQuota(quota);
    }

    void addTagToWorkSpace(String workSpaceName, String tag) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.addTag(tag);
    }

    void removeTagFromWorkSpace(String workSpaceName, String tag) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.removeTag(tag);
    }

    void addFileToWorkSpace(String workSpaceName, File f) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.addFile(f);
    }

    void removeFileFromWorkSpace(String workSpaceName, File f) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.removeFile(f);
    }

    void setWorkSpaceToPublic(String workSpaceName) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.setPublic(true);
    }

    void setWorkSpaceToPrivate(String workSpaceName) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.setPublic(false);
    }

    public void setWorkSpaceName(String oldName, String newName) {
        WorkSpace ws = getWorkspaceByName(oldName);
        ws.setName(newName);
    }

    public void addUserToWorkSpace(String workSpaceName, User u) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
        ws.addPermittedUser(u);
    }

    public void removeUserFromWorkSpace(String workSpaceName, User u) {
        WorkSpace ws = getWorkspaceByName(workSpaceName);
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

    public Map<String, WorkSpace> getOwnedWorkSpaces() {
        return ownedWorkSpaces;
    }

    public Map<String, WorkSpace> getSubscribedWorkSpaces() {
        return subscribedWorkSpaces;
    }


    /*
     * Private methods
     */

    private WorkSpace getWorkspaceByName(String workSpaceName) {
        return ownedWorkSpaces.get(workSpaceName);
    }
}
