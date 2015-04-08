package pt.ulisboa.tecnico.cmov.cmovproject.model;

/**
 * To use a {@link airDeskFile} it is needed to have a implementation for the
 * file abstraction. Any implementation should implement this interface.
 */
public interface FileImpl {
    // TODO: These method signatures are not correct, but we will fix it later
    public void close();
    public String read();
    public void open();
    public void write();
}
