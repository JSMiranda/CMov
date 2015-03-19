package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.Date;

/**
 * This class abstracts a file. Before opening, closing and reading from a
 * file it is needed to set the implementation using {@link pt.ulisboa.tecnico.cmov.cmovproject.model.File#setImpl(FileImpl)}.
 */
public class File {
    private static FileImpl impl;

    private String name;
    private Date lastChanged;

    public File(String name) {
        this.name = name;
        lastChanged = new Date();
    }

    /**
     * Use this method to set an implementation for the file reader
     *
     * @param impl The implementation of the file reader
     */
    public static void setImpl(FileImpl impl) {
        File.impl = impl;
    }

    public void close() {
        impl.close();
    }

    public String read() {
        return impl.read();
    }

    public void open() {
        impl.open();
    }

    /*
     * Getters and setters
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }
}
