package pt.ulisboa.tecnico.cmov.cmovproject.model;

import java.util.Date;

/**
 * This class abstracts a file. Before opening, closing and reading from a
 * file it is needed to set the implementation using {@link File#setImpl(FileImpl)}.
 */
public class File {
    private static FileImpl impl;

    private String name;
    private Date lastChanged;
    private int size;

    public File(String name) {
        this.name = name;
        lastChanged = new Date();
        /* TODO: set "initial" size. The size when the file was loaded
         * (the file might have been created before app initiation)
         */
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

    public void write() {
        impl.write();
        // TODO: Change size
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

    public int getSize() {
        return size;
    }


    /*
     * Private methods
     */

    /**
     * Only the method {@link File#write()} should use this method
     * (and this is why it is private). We are using an attribute
     * to speed up size queries.
     *
     * @param size
     */
    private void setSize(int size) {
        this.size = size;
    }
}
