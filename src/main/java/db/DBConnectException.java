package db;

public class DBConnectException extends Exception {

    public DBConnectException(final String message) {
        super(message);
    }

    public DBConnectException(final Throwable cause) {
        super(cause);
    }

    public DBConnectException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
