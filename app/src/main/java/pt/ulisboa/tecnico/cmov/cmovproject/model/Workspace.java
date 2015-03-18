package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.Collection;

public class Workspace {
    private String name;
    private int quota;
    private Collection<String> tags;
    private Collection<File> files;
    private boolean isPublic;
    private Collection<User> permittedUsers;
}
