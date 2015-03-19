package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A WorkSpace is responsible for maintaining files
 * and maintaining a very simple ACL
 * (a list of permitted users) for itself.
 */
public class WorkSpace {
    private String name;
    private int quota;
    private Collection<String> tags;
    private Collection<File> files;
    private boolean isPublic;
    private Collection<User> permittedUsers;
    private User owner;

    public WorkSpace(String name, int quota, Collection<String> tags, boolean isPublic, User owner) {
        this.name = name;
        this.quota = quota;
        this.tags = tags; // FIXME: make a copy?
        this.files = new ArrayList<File>();
        this.isPublic = isPublic;
        this.permittedUsers = new ArrayList<User>();
        this.owner = owner;
    }

    public int getUsedQuota() {
        int res = 0;
        for (File f : files) {
            res += f.getSize();
        }
        return res;
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

    public User getOwner() {
        return owner;
    }


    /*
     * Package access methods
     */

    void setQuota(int quota) {
        if (quota >= getUsedQuota()) {
            this.quota = quota;
        } else {
            throw new IllegalStateException("Trying to set quota to a value lower than used quota");
        }
    }

    void addTag(String tag) {
        tags.add(tag);
    }

    void removeTag(String tag) {
        tags.remove(tag);
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
