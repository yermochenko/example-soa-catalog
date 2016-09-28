package utils.pool;

public class ConnectionPoolException extends Exception {
	public ConnectionPoolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionPoolException(String message) {
		super(message);
	}
}