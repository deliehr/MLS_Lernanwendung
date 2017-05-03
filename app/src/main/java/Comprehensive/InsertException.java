package Comprehensive;

/**
 * Exception type for sql insertions
 */
public class InsertException extends Exception {
    public static final String standardMessage = "Insertion failed. Returned id: -1";

    // region constructors
    public InsertException(String message) {
        super(message);
    }
    // endregion
}
