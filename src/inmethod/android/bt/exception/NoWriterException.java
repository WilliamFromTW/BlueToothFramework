package inmethod.android.bt.exception;

/**
 * Data format exception , error message can get by method "getErrorMessage".
 * 
 * @author william
 * 
 *
 */
public class NoWriterException extends Exception {

	private String sError = "";

	public NoWriterException() {
	}

	public NoWriterException(String message) {
		super(message);
		sError = message;
	}

	public NoWriterException(Throwable cause) {
		super(cause);
	}

	public NoWriterException(String message, Throwable cause) {
		super(message, cause);
		sError = message;
	}

	public void printStacktrace() {
		System.err.println(sError);
		super.printStackTrace();
	}
}
