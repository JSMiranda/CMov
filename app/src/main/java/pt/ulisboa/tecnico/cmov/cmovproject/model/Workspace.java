package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.ArrayList;
import java.util.Collection;

public class Workspace {
    private String name;
    private int quota;
    private Collection<String> tags;
    private Collection<File> files;
    private boolean isPublic;
    private Collection<User> permittedUsers;
    private User owner;

    public Workspace(String name, int quota, Collection<String> tags, boolean isPublic, User owner) {
        this.name = name;
        this.quota = quota;
        this.tags = tags; // FIXME: make a copy?
        this.files = new ArrayList<File>();
        this.isPublic = isPublic;
        this.permittedUsers = new ArrayList<User>();
        this.owner = owner;
    }

    /*
     * Getters and setters
     */

    public String getName() {
        return name;
    }

    public int getQuota() {
        return quota;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public Collection<File> getFiles() {
        return files;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Collection<User> getPermittedUsers() {
        return permittedUsers;
    }


    void setQuota(int quota) {
        // TODO: verify if it is valid
        this.quota = quota;
    }

    void setTags(Collection<String> tags) {
        this.tags = tags;
    }

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
}
