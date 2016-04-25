package inmethod.android.bt.le;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.BlueToothGlobalSetting;
import inmethod.android.bt.ScanRecord;
import inmethod.android.bt.handler.BlueToothDiscoveryServiceCallbackHandler;
import inmethod.android.bt.interfaces.IBlueToothDiscoveryService;
import inmethod.commons.util.HexAndStringConverter;

public class BlueToothLeDiscoveryService implements IBlueToothDiscoveryService {
	// Debugging
	public final String TAG = BlueToothGlobalSetting.TAG + "/" + getClass().getSimpleName();
	protected Context aContext = null;
	private BlueToothDiscoveryServiceCallbackHandler mHandler;
	private  Handler stopScanHandler;
	protected boolean bRun = false;
	protected  BluetoothAdapter mBluetoothAdapter = null;
	private  BluetoothAdapter.LeScanCallback mLeScanCallback = null;
	// bluetooth set
	protected ArrayList<BTInfo> aOnlineDeviceList = new ArrayList<BTInfo>();
	protected ArrayList<BTInfo> aOneOnlineDeviceList = new ArrayList<BTInfo>();
	protected BTInfo aBTInfo = null;

	protected boolean bDeviceFound = false;
	public static BlueToothLeDiscoveryService aBlueToothLeDiscoveryService = null;

	private boolean isDiscovering = false;
	private boolean bCancelDiscovery = false;
	private Vector<String> aFilter = null;
	private  BluetoothManager bluetoothManager = null;

	private int iDefaultDiscoveryMode = DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY;

	private BlueToothLeDiscoveryService() {
	};

	/**
	 * Singleton Class
	 * 
	 * @return
	 */
	public static BlueToothLeDiscoveryService getInstance() {
		if (aBlueToothLeDiscoveryService == null) {
			aBlueToothLeDiscoveryService = new BlueToothLeDiscoveryService();
			aBlueToothLeDiscoveryService.stopScanHandler = new Handler();
		}
		return aBlueToothLeDiscoveryService;
	}

	/**
	 * Context , must not be null
	 * 
	 * @param aC
	 *            context or activity or application context(service)
	 */
	public void setContext(Context aC) {
		aContext = aC;
		bluetoothManager = (BluetoothManager) aContext.getSystemService(Context.BLUETOOTH_SERVICE);
	}

	/**
	 * call back message handler , must not be null
	 * 
	 * @param mHandler
	 */
	public void setCallBackHandler(BlueToothDiscoveryServiceCallbackHandler mHandler) {
		this.mHandler = mHandler;
	}

	@Override
	public BlueToothDiscoveryServiceCallbackHandler getCallBackHandler() {
		return mHandler;
	}

	/**
	 * check bluetooth device is enable or disable.
	 * 
	 * @return
	 */
	public boolean isBlueToothReady() {
		if (mBluetoothAdapter == null) {
			try {
				mBluetoothAdapter = bluetoothManager.getAdapter();
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
		Log.d(TAG, "prepareBluetoothAdapter");
		if (mBluetoothAdapter == null) {
			try {
				mBluetoothAdapter = bluetoothManager.getAdapter();
				Log.d(TAG, "prepareBLEAdapter");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATUS_NO_BLUETOOTH_MODULE).sendToTarget();
			return false;
		}

		mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
				if (device == null || !isRunning())
					return;
				/*
				List<ScanRecord> aScanRecord = ScanRecord.parseScanRecord(scanRecord);
				for (ScanRecord record : aScanRecord) {
					Log.i(TAG,
							"scan Record Length=" + record.getLength() + ",Type=0x"
									+ HexAndStringConverter.convertHexByteToHexString(record.getType()) + ",Data="
									+ new String(record.getData()) + "(HEX:"
									+ HexAndStringConverter.convertHexByteToHexString(record.getData()) + ")");
				}
*/
				if (filterFoundBTDevice(device.getName())) {
					BTInfo aBTInfo = new BTInfo();
					aBTInfo.setDeviceAddress(device.getAddress());
					aBTInfo.setDeviceName(device.getName());
					aBTInfo.setDeviceBlueToothType(BTInfo.DEVICE_TYPE_LE);
					aBTInfo.setBroadcastData(scanRecord.clone());
					boolean bFound = true;
					for (BTInfo aInfo : aOnlineDeviceList) {
						Log.i(TAG, "aInfo=" + aInfo.getDeviceAddress() + ",aBTInfo=" + aBTInfo.getDeviceAddress());
						if (aInfo.getDeviceAddress().equalsIgnoreCase(aBTInfo.getDeviceAddress())) {
							bFound = false;
						}
					}
					if (bFound) {

						aOnlineDeviceList.add(aBTInfo);
						// Log.i(TAG,"name="+aBTInfo.getDeviceName()+",address="+aBTInfo.getDeviceAddress()+",type="+aBTInfo.getType());
						// Log.i(TAG, "LeScanCallback , get device
						// name="+device.getName()+",address =
						// "+device.getAddress()+",devicelist
						// counter="+aOnlineDeviceList.size());
						if (iDefaultDiscoveryMode == BlueToothLeDiscoveryService.DISCOVERY_MODE_FOUND_AND_CONTINUE_DISCOVERY) {
							bDeviceFound = true;
							Message msg = mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_LIST);
							Bundle bundle = new Bundle();
							aOneOnlineDeviceList.clear();
							aOneOnlineDeviceList.add(aBTInfo);
							bundle.putParcelableArrayList(BlueToothGlobalSetting.BUNDLE_ONLINE_DEVICE_LIST,
									aOneOnlineDeviceList);
							msg.setData(bundle);
							mHandler.sendMessage(msg);
						} else if (iDefaultDiscoveryMode == BlueToothLeDiscoveryService.DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY) {

							if (aOnlineDeviceList.size() == 1) {

								Message msg = mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_LIST);
								Bundle bundle = new Bundle();
								bundle.putParcelableArrayList(BlueToothGlobalSetting.BUNDLE_ONLINE_DEVICE_LIST,
										aOnlineDeviceList);
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}
							bDeviceFound = true;
						}
					}
				}
			}

		};

		return true;
	}

	/**
	 * check this DiscoveryService is running
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
		Log.d(TAG, "stopDiscoveryService()");
		clearData();
		mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STOP_DISCOVERY_SERVICE).sendToTarget();
		bRun = false;
	}

	/**
	 * start service , if bluetooth is not enable , BlueToothLeDiscoveryService will
	 * stop if context or handler is null , BlueToothLeDiscoveryService will stop
	 */
	public void startService() throws Exception {

		if (aContext == null || this.mHandler == null)
			throw new Exception("context or handler cannot be null");
		if (isRunning())
			return;
		aOnlineDeviceList.clear();

		Log.d(TAG, "startDiscoveryService()");
		bRun = true;
		if (!prepareBluetoothAdapter()){
			this.stopService();
		}	
		else {
			if (!mBluetoothAdapter.isEnabled()) {
				mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE).sendToTarget();
				mBluetoothAdapter.enable();
			} else {
				mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS).sendToTarget();
			}
		}
	}

	/**
	 * clear variable
	 */
	protected void clearData() {
		bRun = false;
		bDeviceFound = false;
		if (aOnlineDeviceList != null)
			aOnlineDeviceList.clear();
		if (isDiscovering)
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		isDiscovering = false;
		bCancelDiscovery = true;

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
		if(!isRunning()) return;
		if (mBluetoothAdapter != null) {
			bCancelDiscovery = true;
			mHandler.removeMessages(BlueToothGlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND);
		}

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				Log.d(TAG, "cancelDiscovery()");

				isDiscovering = false;
			}
		}, 10);

	}

	/**
	 * discovery manually
	 */
	public void doDiscovery() {
		bDeviceFound = false;
		aOnlineDeviceList.clear();
		if (mBluetoothAdapter != null) {
			if (isDiscovering())
				cancelDiscovery();
			try {
				Thread.sleep(100);
			} catch (Exception eee) {
			}
		}
		Log.d(TAG,"is running?"+isRunning());
		if( !isRunning()) return;
 		Log.d(TAG, "doDiscovery()");

		try {

			mBluetoothAdapter.startLeScan(mLeScanCallback);
			isDiscovering = true;

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isDiscovering = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					if(!isRunning()) return;
					Log.i(TAG, "device found?" + bDeviceFound);

					if (!bDeviceFound) {
						Message msg = mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND);
						mHandler.sendMessageDelayed(msg, 100);
					}
					mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED).sendToTarget();
				}
			}, 12000);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	/**
	 * set bluetooth deivce name , DiscoveryService will avoid to get bluetooth
	 * device that are not in filter list. Name is case-sensitive
	 * 
	 * @param aVector
	 */
	public void setBlueToothDeviceNameFilter(Vector<String> aVector) {
		aFilter = aVector;
	}

	/**
	 * find which bluetooth device we need to connect
	 * 
	 * @param sBTName
	 * @return
	 */
	private boolean filterFoundBTDevice(String sBTName) {
		if (sBTName != null) {
			if (aFilter != null) {
				for (String sName : aFilter) {
					if (sBTName.indexOf(sName) != -1)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * IBlueToothDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY(default), IBlueToothDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 */
	@Override
	public void setDiscoveryMode(int iMode) {
		iDefaultDiscoveryMode = iMode;
	}

	/**
	 * @return IBlueToothDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY(default), IBlueToothDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 */
	@Override
	public int getDiscoveryMode() {
		return iDefaultDiscoveryMode;
	}

	/**
	 * default is false (DiscoveryService can discover classic , dual and ble
	 * device). true: DiscoveryService can discover ble device only.
	 * 
	 * @param bBLE
	 */
	public void useBLEonly(boolean bBLE) {
		// no use
	}


}