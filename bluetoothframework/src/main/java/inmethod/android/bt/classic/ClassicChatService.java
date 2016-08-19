package inmethod.android.bt.classic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.GlobalSetting;
import inmethod.android.bt.exception.NoBTReaderException;
import inmethod.android.bt.exception.NoWriterException;
import inmethod.android.bt.interfaces.IChatService;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class ClassicChatService implements IChatService {
	// Debugging
	public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();

	// Name for the SDP record when creating server socket
	private static final String NAME_SECURE = "BluetoothChatSecure";
	private static final String NAME_INSECURE = "BluetoothChatInsecure";

	/**
	 * Unique UUID for this application. default is Serial Port Profile
	 * 00001101-0000-1000-8000-00805F9B34FB
	 */
	public static final UUID MY_UUID_SECURE = UUID.fromString(GlobalSetting.SPP_UUID);

	/**
	 * Unique UUID for this application. default is Serial Port Profile
	 * 00001101-0000-1000-8000-00805F9B34FB
	 */
	public static final UUID MY_UUID_INSECURE = UUID.fromString(GlobalSetting.SPP_UUID);

	private static UUID SecureUUID = MY_UUID_SECURE;
	private static UUID InSecureUUID = MY_UUID_INSECURE;

	// Member fields
	private BluetoothAdapter mAdapter;
	private Handler mHandler;
	private AcceptThread mSecureAcceptThread;
	private AcceptThread mInsecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device
	public static final int STATE_LOST = 6; // now connected to a remote device
	// default mode will be insecure mode
	public static final int INSECURE_ACCEPT_MODE = 1;
	public static final int SECURE_ACCEPT_MODE = 2;
	protected int iAcceptMode = INSECURE_ACCEPT_MODE;
	protected boolean bEnableAcceptService = false;

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 */
	public ClassicChatService() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
	}

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 */
	public ClassicChatService(String sSecureUUID, String sInSecureUUID) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		this.SecureUUID = UUID.fromString(sSecureUUID);
		this.InSecureUUID = UUID.fromString(sInSecureUUID);
	}

	public void setHandler(Handler aHandler) {
		this.mHandler = aHandler;
	}

	/**
	 * default mode is INSECURE_ACCEPT_MODE
	 * 
	 * @param iMode
	 *            INSECURE_ACCEPT_MODE or SECURE_ACCEPT_MODE
	 */
	public void setAcceptMode(int iMode) {
		iAcceptMode = iMode;
	}

	/**
	 * default disable
	 * 
	 * @param enable
	 */
	public void enableAcceptService(boolean enable) {
		this.bEnableAcceptService = enable;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	public synchronized void setState(int state) {
		Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(GlobalSetting.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	private void initial() {

		Log.d(TAG, "ClassicChatService initial");
		if (mAdapter == null)
			mAdapter = BluetoothAdapter.getDefaultAdapter();

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {

		Log.d(TAG, "ClassicChatService start");
		initial();

		// Start the thread to listen on a BluetoothServerSocket
		if (bEnableAcceptService) {
			setState(STATE_LISTEN);
			if (iAcceptMode == SECURE_ACCEPT_MODE) {
				if (mSecureAcceptThread == null) {
					mSecureAcceptThread = new AcceptThread(true);
					mSecureAcceptThread.start();
				}
			} else {
				if (mInsecureAcceptThread == null) {
					mInsecureAcceptThread = new AcceptThread(false);
					mInsecureAcceptThread.start();
				}
			}
		}
	}

	public synchronized void connect(String sBluetoothAddress) {
		BluetoothDevice device = mAdapter.getRemoteDevice(sBluetoothAddress);
		connect(device, false);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(BluetoothDevice device, boolean secure) {

		Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device, secure);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {

		Log.d(TAG, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (bEnableAcceptService) {
			if (iAcceptMode == SECURE_ACCEPT_MODE) {
				if (mSecureAcceptThread != null) {
					mSecureAcceptThread.cancel();
					mSecureAcceptThread = null;
				}
			} else {
				if (mInsecureAcceptThread != null) {
					mInsecureAcceptThread.cancel();
					mInsecureAcceptThread = null;
				}
			}
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();
		try {
			Thread.sleep(100);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(GlobalSetting.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(GlobalSetting.DEVICE_NAME, device.getName());
		bundle.putString(GlobalSetting.DEVICE_ADRESS, device.getAddress());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		Message msg2 = mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTED);
		Bundle bundle2 = new Bundle();
		BTInfo aBTInfo = new BTInfo();
		aBTInfo.setDeviceName(device.getName());
		aBTInfo.setDeviceAddress(device.getAddress());
		bundle2.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
		msg2.setData(bundle2);
		mHandler.sendMessage(msg2);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {

		Log.d(TAG, "ClassicChatService synchronized stop");
		setState(STATE_NONE);

		if (mConnectThread != null) {
			// System.out.println("try to do mConnectThread.cancel();");
			mConnectThread.cancel();
			// System.out.println("finish mConnectThread.cancel();");
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			// System.out.println("try to do mConnectedThread.cancel();");
			mConnectedThread.cancel();
			// System.out.println("mConnectedThread.cancel();");
			mConnectedThread = null;
		}

		if (bEnableAcceptService) {
			if (iAcceptMode == SECURE_ACCEPT_MODE) {
				if (mSecureAcceptThread != null) {
					// System.out.println("try to do
					// mSecureAcceptThread.cancel();");
					mSecureAcceptThread.cancel();
					// System.out.println("mSecureAcceptThread.cancel();");
					mSecureAcceptThread = null;
				}
			} else {
				if (mInsecureAcceptThread != null) {
					// System.out.println("try to do
					// mInsecureAcceptThread.cancel();");
					mInsecureAcceptThread.cancel();
					// System.out.println("mInsecureAcceptThread.cancel();");
					mInsecureAcceptThread = null;
				}
			}
		}

		if (mAdapter != null)
			mAdapter = null;
		// System.out.println("mAdapter=null;");

	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// System.out.println("connection failed");
		mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_FAIL).sendToTarget();
		try {
			// if(
			// ClassicChatService.this.mState==ClassicChatService.STATE_CONNECTED)
			ClassicChatService.this.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST).sendToTarget();
		/*
		 * Bundle bundle = new Bundle(); bundle.putString(BlueToothGlobal.TOAST,
		 * "Device connection was lost"); msg.setData(bundle);
		 * mHandler.sendMessage(msg);
		 */
		// Start the service over to restart listening mode
		try {
			if (ClassicChatService.this.mState == ClassicChatService.STATE_CONNECTED)
				ClassicChatService.this.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// ClassicChatService.this.start();
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mmServerSocket;
		private String mSocketType;

		public AcceptThread(boolean secure) {
			BluetoothServerSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Create a new listening server socket
			try {
				if (secure) {
					tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, SecureUUID);
				} else {
					tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, InSecureUUID);
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {

			Log.d(TAG, "Socket Type: " + mSocketType + "BEGIN mAcceptThread" + this);
			setName("AcceptThread" + mSocketType);

			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (ClassicChatService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice(), mSocketType);
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}

			Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

		}

		public void cancel() {

			Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				if (secure) {
					tmp = device.createRfcommSocketToServiceRecord(SecureUUID);
					/*
					 * try{ Method m =
					 * device.getClass().getMethod("createRfcommSocket", new
					 * Class[]{int.class}); tmp =
					 * (BluetoothSocket)m.invoke(device, Integer.valueOf(3));
					 * }catch(Exception ee){ ee.printStackTrace(); }
					 */
				} else {
					tmp = device.createInsecureRfcommSocketToServiceRecord(InSecureUUID);
					/*
					 * try{ Method m =
					 * device.getClass().getMethod("createRfcommSocket", new
					 * Class[]{int.class}); tmp =
					 * (BluetoothSocket)m.invoke(device, Integer.valueOf(3));
					 * }catch(Exception ee){ ee.printStackTrace(); }
					 */
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {

			setName("ConnectThread" + mSocketType);

			// Make a connection to the BluetoothSocket
			try {
				Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType + ",device name=" + mmDevice.getName()
						+ ",address=" + mmDevice.getAddress());
				mmSocket.connect();
				Log.i(TAG, "END mConnectThread SocketType:" + mSocketType);
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e2);
				}
				Log.e(TAG, "mmSocket.connect() failed");
				// e.printStackTrace();
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (ClassicChatService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			/*
			 * try { mmSocket.close(); System.out.println( "close() of connect "
			 * + mSocketType + " socket failed"); } catch (Exception e) {
			 * System.out.println( "close() of connect " + mSocketType +
			 * " socket failed"); // e.printStackTrace(); }
			 */
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read();
					if (bytes != -1)
						mHandler.obtainMessage(GlobalSetting.MESSAGE_READ, bytes, -1, null).sendToTarget();
					/*
					 * // Read from the InputStream bytes =
					 * mmInStream.read(buffer);
					 * 
					 * // Send the obtained bytes to the UI Activity
					 * mHandler.obtainMessage(BlueToothGlobal.MESSAGE_READ,
					 * bytes, -1, buffer) .sendToTarget();
					 */
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}

		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				mmOutStream.flush();
				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(GlobalSetting.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	@Override
	public void setBlueToothAdapter(BluetoothAdapter aBlueToothAdapter) {
		mAdapter = aBlueToothAdapter;

	}

	@Override
	/**
	 * @param objWriterChannel
	 *            no use in this class
	 */
	public void write(byte[] out, Object objWriterChannel) throws NoWriterException {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	@Override
	public BluetoothAdapter getBlueToothAdapter() {
		return mAdapter;
	}

	@Override
	public void read(Object objReaderChannel) throws NoBTReaderException {
		// TODO Auto-generated method stub

	}

	/**
	 * Not Support Simulation mode
	 * @param data
	 */
	@Override
	public void setSimulationResponsedData(byte[] data) {

	}
	/**
	 * Not Support Simulation mode
	 * @param sUUID
	 */
	@Override
	public void setSimulationResponsedUUID(String sUUID) {

	}
}
