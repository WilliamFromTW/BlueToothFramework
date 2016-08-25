package inmethod.android.bt.interfaces;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

import inmethod.android.bt.exception.NoBTReaderException;
import inmethod.android.bt.exception.NoWriterException;

public interface IChatService {

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device
	public static final int STATE_LOST = 6; // now connected to a remote device

	/**
	 * some message feedback.
	 * 
	 * @param aHandler
	 */
	public void setHandler(Handler aHandler);

	public void setBlueToothAdapter(BluetoothAdapter aBlueToothAdapter);

	public BluetoothAdapter getBlueToothAdapter();

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	public void setState(int state);

	/**
	 * Return the current connection state.
	 */
	public int getState();

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public void start();

	/**
	 * connect to a remote device.
	 * 
	 * @param sBluetoothAddress
	 */
	public void connect(String sBluetoothAddress);

	/**
	 * Stop all threads
	 */
	public void stop();

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @param objWriterChannel
	 *            specify writer channel
	 * 
	 */
	public void write(byte[] out, Object objWriterChannel) throws NoWriterException;

	/**
	 * Read characteristic data. Only for BLE .
	 * 
	 * @param objReaderChannel
	 *            must be Characteristic or String(Characteristic UUID)
	 * @throws NoBTReaderException
	 */
	public void read(Object objReaderChannel) throws NoBTReaderException;
}
