package pt.ulisboa.tecnico.cmov.cmovproject.exception;

public class InvalidQuotaException extends Exception {
    private static long serialVersionUID = 1L;

    public InvalidQuotaException(long min, long max) {
        super("The value should be between " + min + " and " + max);
    }
}
