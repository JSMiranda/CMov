package pt.ulisboa.tecnico.cmov.cmovproject.exception;

public class WorkspaceAlreadyExistsException extends Exception {
    private static long serialVersionUID = 1L;

    public WorkspaceAlreadyExistsException(String name) {
        super("OwnedWorkspace named \"" + name + "\" already exists.");
    }
}
