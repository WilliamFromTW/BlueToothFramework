package inmethod.android.bt.exception;

/**
 * Data format exception , error message can get by method "getErrorMessage".
 * 
 * @author william
 * 
 *
 */
public class NoUuidException extends Exception {

	private String sError = "";

	public NoUuidException() {
	}

	public NoUuidException(String message) {
		super(message);
		sError = message;
	}

	public NoUuidException(Throwable cause) {
		super(cause);
	}

	public NoUuidException(String message, Throwable cause) {
		super(message, cause);
		sError = message;
	}

	public void printStacktrace() {
		System.err.println(sError);
		super.printStackTrace();
	}
}
