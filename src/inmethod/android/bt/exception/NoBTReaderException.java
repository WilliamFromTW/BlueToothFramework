package inmethod.android.bt.exception;

/**
 * Data format exception , error message can get by method "getErrorMessage".
 * 
 * @author william
 * 
 *
 */
public class NoBTReaderException extends Exception {

	private String sError = "";

	public NoBTReaderException() {
	}

	public NoBTReaderException(String message) {
		super(message);
		sError = message;
	}

	public NoBTReaderException(Throwable cause) {
		super(cause);
	}

	public NoBTReaderException(String message, Throwable cause) {
		super(message, cause);
		sError = message;
	}

	public void printStacktrace() {
		System.err.println(sError);
		super.printStackTrace();
	}
}
