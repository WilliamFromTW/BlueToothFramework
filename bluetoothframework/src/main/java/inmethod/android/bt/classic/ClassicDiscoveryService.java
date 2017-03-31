package inmethod.android.bt.classic;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import inmethod.android.bt.BTInfo;
import inmethod.android.bt.GlobalSetting;
import inmethod.android.bt.handler.DiscoveryServiceCallbackHandler;
import inmethod.android.bt.interfaces.IDiscoveryService;

public class ClassicDiscoveryService implements IDiscoveryService {
	// Debugging
	public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();
	protected Context aContext = null;
	private DiscoveryServiceCallbackHandler mHandler;
	protected static final boolean D = true;
	protected boolean bRun = false;
	protected static BluetoothAdapter mBluetoothAdapter = null;

	// bluetooth set
	protected Set<BluetoothDevice> aPairedBT = null;
	protected ArrayList<BTInfo> aPairedBTList = null;
	protected ArrayList<BTInfo> aOnlineDeviceList = new ArrayList<BTInfo>();
	private ArrayList<BTInfo> aTmpDeviceList = new ArrayList<BTInfo>();
	protected BTInfo aBTInfo = null;

	protected boolean bAutoPair = false;
	protected boolean bDeviceFound = false;
	public static ClassicDiscoveryService aClassicDiscoveryService = null;
	private boolean isDiscovering = false;
	public final static int DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY = IDiscoveryService.DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY;
	public final static int DISCOVERY_MODE_FOUND_AND_CONTINUE_DISCOVERY = IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CONTINUE_DISCOVERY;

	private int iDefaultDiscoveryMode = DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY;
	private Vector<String> aFilter = null;
	private boolean bCancelDiscovery = false;

	public static final int DISCOVER_BLUETOOTH_CLASSIC_ONLY = 1;
	public static final int DISCOVER_BLUETOOTH_CLASSIC_AND_LE = 2;
	private int iDiscoverFilter = DISCOVER_BLUETOOTH_CLASSIC_AND_LE;
	private boolean bUseBLEonly = false;

	/**
	 * default is ClassicDiscoveryService.DISCOVER_BLUETOOTH_CLASSIC_AND_LE.
	 * 
	 * @param iDiscoverFilter
	 *            ClassicDiscoveryService.DISCOVER_BLUETOOTH_CLASSIC_ONLY: find
	 *            classic bluetooth only ,
	 *            ClassicDiscoveryService.DISCOVER_BLUETOOTH_CLASSIC_AND_LE: find
	 *            classic bluetooth and low energy device both
	 */
	public void setDiscoverFilter(int iDiscoverFilter) {
		this.iDiscoverFilter = iDiscoverFilter;
	}

	/**
	 * ClassicDiscoveryService.DISCOVER_BLUETOOTH_CLASSIC_ONLY: find classic
	 * bluetooth only ,
	 * ClassicDiscoveryService.DISCOVER_BLUETOOTH_CLASSIC_AND_LE: find classic
	 * bluetooth and low energy device both.
	 * 
	 * @return
	 */
	public int getDiscoverFilter() {
		return this.iDiscoverFilter;
	}

	private ClassicDiscoveryService() {
	};

	/**
	 * Singleton Class
	 * 
	 * @return
	 */
	public static ClassicDiscoveryService getInstance() {
		if (aClassicDiscoveryService == null)
			aClassicDiscoveryService = new ClassicDiscoveryService();
		return aClassicDiscoveryService;
	}

	/**
	 * Context , must not be null
	 * 
	 * @param aC
	 *            context or activity or application context(service)
	 */
	public void setContext(Context aC) {
		aContext = aC;
	}

	/**
	 * call back message handler , must not be null
	 * 
	 * @param mHandler
	 */
	public void setCallBackHandler(DiscoveryServiceCallbackHandler mHandler) {
		this.mHandler = mHandler;
	}

	@Override
	public DiscoveryServiceCallbackHandler getCallBackHandler() {
		return mHandler;
	}

	/**
	 * default is false (ClassicDiscoveryService can discover classic , dual and ble
	 * device). true: ClassicDiscoveryService can discover ble device only.
	 * 
	 * @param bBLE
	 */
	public void useBLEonly(boolean bBLE) {
		bUseBLEonly = bBLE;
	}

	/**
	 * no use in classic bluetooth.
	 * @param iMilliseconds
     */
	@Override
	public void setScanTimeout(int iMilliseconds) {

	}

	/**
	 *  no use in classic bluetooth
	 * @return
     */
	@Override
	public int getScanTimeout() {
		return 0;
	}

	/**
	 * check bluetooth device is enable or disable.
	 * 
	 * @return
	 */
	public boolean isBlueToothReady() {
		if (mBluetoothAdapter == null) {
			try {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		if (mBluetoothAdapter == null)
			return false;
		else
			return true;
	}



	private boolean prepareBluetoothAdapter() {
		// Get local Bluetooth adapter
		if (mBluetoothAdapter == null) {
			try {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_NO_BLUETOOTH_MODULE).sendToTarget();
			return false;
		}
		aPairedBT = mBluetoothAdapter.getBondedDevices();
		aPairedBTList = new ArrayList<BTInfo>();
		if (aPairedBT.size() > 0) {
			for (BluetoothDevice device : aPairedBT) {
				aBTInfo = new BTInfo();
				aBTInfo.setDeviceName(device.getName());
				aBTInfo.setDeviceAddress(device.getAddress());
				aPairedBTList.add(aBTInfo);
			}
		}

		try {
			Log.d(TAG, "for safety reason , unregisterReceiver before registerReceiver!");
			aContext.unregisterReceiver(mReceiver);
		} catch (Exception ex) {
			// ex.printStackTrace();
		}

		IntentFilter state_change_filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		try {
			aContext.registerReceiver(mReceiver, state_change_filter);
		} catch (Exception ex) {
			// ex.printStackTrace();
		}

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		try {
			aContext.registerReceiver(mReceiver, filter);
		} catch (Exception ex) {
			// ex.printStackTrace();
		}

		// Register for broadcasts when discovery has finished
		IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		try {
			aContext.registerReceiver(mReceiver, filter3);
		} catch (Exception ex) {
			// ex.printStackTrace();
		}

		IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		try {
			aContext.registerReceiver(mReceiver, filter4);
		} catch (Exception ex) {
			// ex.printStackTrace();
		}

		return true;
	}

	/**
	 * check this ClassicDiscoveryService is running
	 * 
	 * @return true : running , false not running
	 */
	public boolean isRunning() {
		return bRun;
	}

	/**
	 * stop service
	 */
	public void stopService() {
		if (bRun) {
			Log.d(TAG, "stop Discovery service()");
			if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
				if (mBluetoothAdapter.isDiscovering())
					mBluetoothAdapter.cancelDiscovery();
			}
			clearData();
			if (mHandler != null)
				mHandler.obtainMessage(GlobalSetting.MESSAGE_STOP_DISCOVERY_SERVICE).sendToTarget();
			bRun = false;
			bCancelDiscovery = false;
			try {
				aContext.unregisterReceiver(mReceiver);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * start service , if bluetooth is not enable , ClassicDiscoveryService will
	 * stop if context or handler is null , ClassicDiscoveryService will stop
	 */
	public void startService() throws Exception {
		if (!bRun) {
			if (aContext == null || this.mHandler == null)
				throw new Exception("context or handler cannot be null");

			Log.d(TAG, "start Discovery Service()");
			bRun = true;
			if (!prepareBluetoothAdapter())
				this.stopService();
			else {
				if (!mBluetoothAdapter.isEnabled()) {
					clearData();
					mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE).sendToTarget();
				} else {
					mHandler.obtainMessage(GlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS).sendToTarget();
				}
			}
		}
	}

	/**
	 * clear variable
	 */
	protected void clearData() {
		bRun = false;
		if (aOnlineDeviceList != null)
			aOnlineDeviceList.clear();
		if (aTmpDeviceList != null)
			aTmpDeviceList.clear();
	}


	/***
	 * default is
	 * ClassicDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 * 
	 * @param iMode
	 *            one of ClassicDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 *            ,ClassicDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 */
	public void setDiscoveryMode(int iMode) {
		iDefaultDiscoveryMode = iMode;
	}

	/**
	 * get discovery mode
	 * 
	 * @return one of
	 *         ClassicDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY,ClassicDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 */
	public int getDiscoveryMode() {
		return iDefaultDiscoveryMode;
	}

	/**
	 * check this class is doing discovery
	 * 
	 * @return
	 */
	public boolean isDiscovering() {
		return isDiscovering;
	}

	public void cancelDiscovery() {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
			bCancelDiscovery = true;
			// bDeviceFound = false;
			mBluetoothAdapter.cancelDiscovery();
		}
		if (D)
			Log.d(TAG, "cancelDiscovery()");
		bRun = true;
		// aTmpDeviceList.clear();
		isDiscovering = false;

	}

	/**
	 * discovery manually
	 */
	public void doDiscovery() {
		bDeviceFound = false;
		if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
			cancelDiscovery();
			try {
				Thread.sleep(100);
			} catch (Exception eee) {
			}
		}
		if( !isRunning()) return;
		if (D)
			Log.d(TAG, "doDiscovery()");
		bRun = true;
		aTmpDeviceList.clear();
		try {
			// if(!mBluetoothAdapter.isDiscovering() )
			mBluetoothAdapter.startDiscovery();
			isDiscovering = true;
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	/**
	 * set bluetooth deivce name , ClassicDiscoveryService will avoid to get bluetooth
	 * device that are not in filter list. Name is case-sensitive
	 * 
	 * @param aVector
	 */
	public void setBlueToothDeviceNameFilter(Vector<String> aVector) {
		aFilter = aVector;
	}

	/**
	 * Find which bluetooth device we need to connect.
	 * String maybe Device name or Mac Address(Mac Address will remove ":" character.
	 *
	 * @param sBTName
	 * @return
	 */
	private boolean filterFoundBTDevice(String sBTName) {
		String sLocalBTName = sBTName.replace(":","");

		if (sLocalBTName != null) {
			if (aFilter != null) {
				for (String sName : aFilter) {
					if (sLocalBTName.toUpperCase().indexOf(sName.toUpperCase()) != -1)
						return true;
				}
			}
		}
		return false;
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			String action = intent.getAction();
			if (action == null)
				return;

			if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
				Log.i(TAG, "device is going to disconnect!");
				Message aMessage = mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_DISCONNECT_REQUESTED, 1, -1);
				Bundle aBundle = new Bundle();
				aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aMessage.setData(aBundle);
				mHandler.sendMessage(aMessage);
				return;
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) { // When
																		// discovery
																		// finds
																		// a
																		// device
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (device != null && device.getName() == null) {
					return;
				}


				Log.i(TAG, "check bt device name = " + device.getName() + ",Mac Address=" + device.getAddress());
				if (device != null && filterFoundBTDevice(device.getName())) {
					if (iDefaultDiscoveryMode == ClassicDiscoveryService.DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY) {
						if (mBluetoothAdapter.isDiscovering())
							mBluetoothAdapter.cancelDiscovery();
						isDiscovering = false;
					}
					bDeviceFound = true;
					BTInfo aBTInfo = new BTInfo();
					aBTInfo.setDeviceAddress(device.getAddress());
					aBTInfo.setDeviceName(device.getName());
					try {
						if (bUseBLEonly == false && device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
							Log.i(TAG,
									"ACTION =" + action + ",name = " + device.getName() + ",bluetooth type = classic");
							aBTInfo.setDeviceBlueToothType(BluetoothDevice.DEVICE_TYPE_CLASSIC);
						} else if (device.getType() == BTInfo.DEVICE_TYPE_LE) {
							Log.i(TAG, "ACTION =" + action + ",name = " + device.getName()
									+ ",bluetooth type = Low energy");
							if (getDiscoverFilter() == DISCOVER_BLUETOOTH_CLASSIC_AND_LE) {
								aBTInfo.setDeviceBlueToothType(BTInfo.DEVICE_TYPE_LE);
							} else {
								Log.i(TAG,
										"Bluetooth LE device rejected! because return value getDiscoveryBluetoothType() is not DISCOVER_BLUETOOTH_CLASSIC_AND_LE");
								return;
							}

						} else if (bUseBLEonly == false && device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL) {
							Log.i(TAG, "ACTION =" + action + ",name = " + device.getName() + ",bluetooth type = Dual");
							aBTInfo.setDeviceBlueToothType(BluetoothDevice.DEVICE_TYPE_DUAL);
						} else {
							Log.i(TAG,
									"ACTION =" + action + ",name = " + device.getName() + ",bluetooth type = unknown");
							return;
						}
					} catch (NoSuchMethodError ee) {
						aBTInfo.setDeviceBlueToothType(BluetoothDevice.DEVICE_TYPE_CLASSIC);

					}

					aOnlineDeviceList = null;
					aOnlineDeviceList = new ArrayList<BTInfo>();
					aOnlineDeviceList.add(aBTInfo);
					Message msg = mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_LIST);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList(GlobalSetting.BUNDLE_ONLINE_DEVICE_LIST, aOnlineDeviceList);
					msg.setData(bundle);
					mHandler.sendMessage(msg);

				}
			} else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
				;// System.out.println("ACTION_NAME_CHANGED, skip...");
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if (bCancelDiscovery) {
					bCancelDiscovery = false;
					if (!bDeviceFound)
						return;
				}
				Log.d(TAG, "ACTION_DISCOVERY_FINISHED");
				if (!bDeviceFound) {
					mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND).sendToTarget();
					bDeviceFound = false;
				}
				
				mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED).sendToTarget();

				try {
					Thread.sleep(100);
				} catch (Exception ex) {
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) { // When
																					// discovery
																					// finds
																					// a
																					// device
				;// Log.d(TAG, "discovery started!");
			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (state) {
				case BluetoothAdapter.STATE_OFF:
					Log.i(TAG, "Bluetooth off!");
					Message msg = mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_BLUETOOTH_OFF);
					mHandler.sendMessage(msg);

					if (ClassicDiscoveryService.this.isRunning()) {
						ClassicDiscoveryService.this.stopService();
					}
					try {
						if (aContext != null && mReceiver != null)
							aContext.unregisterReceiver(mReceiver);
					} catch (Exception ex) {
					}

					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					break;
				case BluetoothAdapter.STATE_ON:
					Log.i(TAG, "Bluetooth on!");
					try {
						ClassicDiscoveryService.this.stopService();
						ClassicDiscoveryService.this.startService();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					break;
				}
			}
		}
	};

}
