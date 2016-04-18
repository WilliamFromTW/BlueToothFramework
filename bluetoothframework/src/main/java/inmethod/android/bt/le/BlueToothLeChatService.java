package inmethod.android.bt.le;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.BlueToothGlobalSetting;
import inmethod.android.bt.classic.BlueToothChatService;
import inmethod.android.bt.exception.NoBTReaderException;
import inmethod.android.bt.exception.NoWriterException;
import inmethod.android.bt.interfaces.IBlueToothChatService;
import inmethod.commons.util.HexAndStringConverter;

/** 
 *
 */
public class BlueToothLeChatService implements IBlueToothChatService {
	// Debugging
	public final String TAG = BlueToothGlobalSetting.TAG + "/" + getClass().getSimpleName();
	private static final boolean D = true;

	// Member fields
	private Handler mHandler;
	private Context context;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing

	public final static String ACTION_GATT_CONNECTED = "inmethod.android.bt.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "inmethod.android.bt.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "inmethod.android.bt.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "inmethod.android.bt.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "inmethod.android.bt.EXTRA_DATA";

	public static final int STATE_DISCONNECTED = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;
	public static final int STATE_GATT_SERVICES_DISCOVERED = 2001;
	public static final int STATE_LOST = 6;

	private BluetoothGatt mBluetoothGatt;
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;

	private BluetoothGattCallback mGattCallback = null;
	private ArrayList<String> aSetNotifyOrIndicatorCharacteristicListenerUUID = null;
	private HashMap<String,BluetoothGattCharacteristic> allCharacteristic = null;
	private boolean bIsWrite = false;
	private static final Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();

	/**
	 * 
	 * @param adapter
	 * @param context
	 * @param aSetNotifyOrIndicatorCharacteristicListenerUUID
	 *            these characteristic is used to enable nofity or indicator
	 */
	public BlueToothLeChatService(BluetoothAdapter adapter, Context context,
			ArrayList<String> aSetNotifyOrIndicatorCharacteristicListenerUUID) {
		mBluetoothAdapter = adapter;
		mState = STATE_NONE;
		this.context = context;
		this.aSetNotifyOrIndicatorCharacteristicListenerUUID = aSetNotifyOrIndicatorCharacteristicListenerUUID;
	}

	private BlueToothLeChatService() {
	};

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	public synchronized void setState(int state) {
		// Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	private void initial() {
		Log.d(TAG, "BlueToothChatService initial");
	}

	private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
		byte[] data = characteristic.getValue();
		if (data != null && data.length > 0) {

			for (byte bytes : data)
				mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_READ, bytes, -1, characteristic.getUuid().toString())
						.sendToTarget();
		}
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {
		Log.d(TAG, "BlueToothChatService start");
		initial();
	}

	/**
	 * Stop all threads
	 */
	public void stop() {

		Log.d(TAG, "BlueToothLeChatService synchronized stop");
		setState(STATE_NONE);

		if (mBluetoothAdapter == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
		}
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST).sendToTarget();
		}
	}

	/**
	 * @param address
	 *            bluetooth mac address
	 */
	public void connect(String address) {
		try {
			setState(STATE_CONNECTING);
			if (mBluetoothAdapter == null || address == null) {
				Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
			}
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

			if (device == null) {
				Log.w(TAG, "Device not found.  Unable to connect.");
			}
			// We want to directly connect to the device, so we are setting the
			// autoConnect
			// parameter to false.
			mGattCallback = getGattCallback();
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
				mBluetoothGatt = null;
			}
			mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
			Log.d(TAG, "Trying to create a new gatt connection.");
			mState = STATE_CONNECTING;
		} catch (Exception ex) {
			Log.e(TAG, "device.connectGatt failed!");
			mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_FAIL).sendToTarget();
			setState(STATE_LOST);
		}
	}

	/**
	 * Write data to remote device .
	 * 
	 * @param aWriterUUIDString
	 *            aWriterUUIDString is UUID string that used to get
	 *            BluetoothGattCharacteristic writer object.
	 * @param out
	 * @see getBluetoothGatt()
	 */
	public void write(byte[] out, Object aWriterUUIDString) throws NoWriterException {
		Log.d(TAG, "write(byte[] out, Object aWriterUUIDString), aWriterUUIDString= "+aWriterUUIDString+",out="+HexAndStringConverter.convertHexByteToHexString(out) );
		
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.e(TAG, "BluetoothAdapter not initialized");
			return;
		}
		if (aWriterUUIDString != null && aWriterUUIDString instanceof BluetoothGattCharacteristic) {
			((BluetoothGattCharacteristic) aWriterUUIDString).setValue(out);
			mBluetoothGatt.writeCharacteristic(((BluetoothGattCharacteristic) aWriterUUIDString));
		} else if (aWriterUUIDString != null && aWriterUUIDString instanceof String) {
			BluetoothGattCharacteristic aCustomWriter = allCharacteristic.get(aWriterUUIDString);
			if (aCustomWriter != null) {
				aCustomWriter.setValue(out);
				mBluetoothGatt.writeCharacteristic(aCustomWriter);
			} else
				throw new NoWriterException(
						"BluetoothGattCharacteristic not found according to UUID=" + aWriterUUIDString);
		} else {
			throw new NoWriterException("writer is null or unexpected error! UUID =" + aWriterUUIDString);
		}
	}

	private BluetoothGattCallback getGattCallback() {
		return new BluetoothGattCallback() {
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				Log.i(TAG, "BluetoothGattCallback connection state=" + newState);
				// this means try to connect but failed!
				Log.i(TAG, "Connected to GATT server. status=" + status);
				if (status != BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
					mState = STATE_DISCONNECTED;
					setState(mState);
					try {
						BlueToothLeChatService.this.stop();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					Log.i(TAG, "Disconnected from GATT server.");
				} else if (newState == BluetoothProfile.STATE_CONNECTED && mState != STATE_CONNECTED) {
					// Attempts to discover services after successful
					// connection.
					mBluetoothAdapter.cancelDiscovery();
					mState = STATE_CONNECTED;
					setState(STATE_CONNECTED);

					Log.i(TAG, "Attempting to start service discovery and enable all notifications or indicators:"
							+ gatt.discoverServices());
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					mState = STATE_DISCONNECTED;
					setState(mState);
					try {
						BlueToothLeChatService.this.stop();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					Log.i(TAG, "Disconnected from GATT server.");
				}
			}

			/**
			 * get gatt object. use this method can get more information or
			 * object , ex: get all service or characteristic object.
			 * 
			 * @return
			 */
			public BluetoothGatt getBluetoothGatt() {
				return mBluetoothGatt;
			}

			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				// InnerHandler innerHandler = new InnerHandler();
				if( allCharacteristic ==null ) allCharacteristic = new HashMap<String,BluetoothGattCharacteristic>() ;
				else allCharacteristic.clear();
				Log.e(TAG, "onServicesDiscovered gatt=" + gatt + ",status=" + status);
				if (status == BluetoothGatt.GATT_SUCCESS) {
					if (gatt == null) {
						Log.e(TAG, "gatt is null");
						Message msg1 = mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_FAIL);
						mHandler.sendMessage(msg1);
						return;
					}
					
				
					
					for (BluetoothGattService service : mBluetoothGatt.getServices()) {
						Log.d(TAG, "=>service uuid = " + service.getUuid());
                         
						for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
							Log.d(TAG, "==>characteristic uuid under service= " + characteristic.getUuid());
							allCharacteristic.put(characteristic.getUuid().toString() , characteristic);
							if (aSetNotifyOrIndicatorCharacteristicListenerUUID != null
									&& aSetNotifyOrIndicatorCharacteristicListenerUUID.size() > 0) {
								for (String sUUID : aSetNotifyOrIndicatorCharacteristicListenerUUID) {
									if (characteristic.getUuid().toString().equalsIgnoreCase(sUUID)) {
										if (isCharacterisiticNotifiable(characteristic))
											setCharacteristicNotification(characteristic, true);
										if (isCharacterisiticIndicator(characteristic))
											setCharacteristicIndicator(characteristic, true);
									}
								}
							}

						}
					}
				} else {
					Log.w(TAG, "onServicesDiscovered failed! status = " + status);
					Message msg1 = mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_FAIL);
					mHandler.sendMessage(msg1);
				}
			}

			@Override
			public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
					int status) {
				Log.i(TAG, "characteristic uuid = " + characteristic.getUuid() + ",onCharacteristicWrite , status = "
						+ status);
				bIsWrite = false;
			}

			@Override
			public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
					int status) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
				}
			}

			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
				// Log.i(TAG,"onCharacteristicChanged,uuid="+characteristic.getUuid()
				// );
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}

			@Override
			public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

				Log.i(TAG,
						"onDescriptorWrite , descriptor's owner characteristic="
								+ descriptor.getCharacteristic().getUuid() + ", descriptor uuid = "
								+ descriptor.getUuid() + ",status=" + status);
				if (status == BluetoothGatt.GATT_SUCCESS) {
					Message aMessage = mHandler
							.obtainMessage(BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS, 1, -1);
					Bundle aBundle = new Bundle();
					aBundle.putString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
							descriptor.getCharacteristic().getUuid().toString());
					aMessage.setData(aBundle);
					mHandler.sendMessage(aMessage);
				} else {
					Message aMessage = mHandler
							.obtainMessage(BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL, 1, -1);
					Bundle aBundle = new Bundle();
					aBundle.putString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
							descriptor.getCharacteristic().getUuid().toString());
					aMessage.setData(aBundle);
					mHandler.sendMessage(aMessage);
				}

				bIsWrite = false;
				nextWrite();
			}
		};
	}

	private synchronized void writeDescriptor(Object o) {
		if (sWriteQueue.isEmpty() && !bIsWrite) {
			doWriteDescibe(o);
		} else {
			sWriteQueue.add(o);
		}
	}

	private synchronized void nextWrite() {
		if (!sWriteQueue.isEmpty() && !bIsWrite) {
			doWriteDescibe(sWriteQueue.poll());
		}
	}

	private synchronized void doWriteDescibe(Object o) {
		if (o instanceof BluetoothGattCharacteristic) {
			bIsWrite = true;
			mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
		} else if (o instanceof BluetoothGattDescriptor) {
			bIsWrite = true;
			mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) o);
		} else {
			nextWrite();
		}
	}

	public static boolean isCharacteristicWriteable(BluetoothGattCharacteristic pChar) {
		return (pChar.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE
				| BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
	}

	/**
	 * @return Returns <b>true</b> if property is Readable
	 */
	public static boolean isCharacterisitcReadable(BluetoothGattCharacteristic pChar) {
		return ((pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
	}

	/**
	 * @return Returns <b>true</b> if property is supports notification
	 */
	public boolean isCharacterisiticNotifiable(BluetoothGattCharacteristic pChar) {
		return (pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
	}

	public boolean isCharacterisiticIndicator(BluetoothGattCharacteristic pChar) {
		return (pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;
	}

	public void setCharacteristicIndicator(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		try {
			mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID.fromString(BlueToothGlobalSetting.Client_Characteristic_Configuration));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

			writeDescriptor(descriptor);
		} catch (Exception ee) {
			ee.printStackTrace();
		}

	}

	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		try {
			mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID.fromString(BlueToothGlobalSetting.Client_Characteristic_Configuration));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			writeDescriptor(descriptor);

		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	@Override
	public void setHandler(Handler aHandler) {
		mHandler = aHandler;
	}

	@Override
	public void setBlueToothAdapter(BluetoothAdapter aBlueToothAdapter) {
		mBluetoothAdapter = aBlueToothAdapter;
	}

	@Override
	public BluetoothAdapter getBlueToothAdapter() {
		return mBluetoothAdapter;
	}

	@Override
	public void read(Object objReaderChannel) throws NoBTReaderException {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.e(TAG, "BluetoothAdapter not initialized");
			return;
		}
		if (objReaderChannel != null && objReaderChannel instanceof BluetoothGattCharacteristic) {
			mBluetoothGatt.readCharacteristic(((BluetoothGattCharacteristic) objReaderChannel));
		} else if (objReaderChannel != null && objReaderChannel instanceof String) {
			BluetoothGattCharacteristic aCustomReader = allCharacteristic.get(objReaderChannel);
			if (aCustomReader != null) {
				mBluetoothGatt.readCharacteristic(aCustomReader);
			}
		} else {
			throw new NoBTReaderException("writer is null or unexpected error! =" + objReaderChannel);
		}

	}

}
