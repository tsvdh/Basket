package server;

public class ServerConnectionException extends Exception {

    public ServerConnectionException(final String message) {
        super(message);
    }

    public ServerConnectionException(final Throwable cause) {
        super(cause);
    }

    public ServerConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
