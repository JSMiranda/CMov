package pt.ulisboa.tecnico.cmov.cmovproject.exception;

public class FileAlreadyExistsException extends Exception {
    private static long serialVersionUID = 1L;

    public FileAlreadyExistsException(String name) {
        super("File named \"" + name + "\" already exists.");
    }
}
