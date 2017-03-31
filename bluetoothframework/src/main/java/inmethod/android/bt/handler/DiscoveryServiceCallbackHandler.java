package inmethod.android.bt.handler;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.GlobalSetting;

public abstract class DiscoveryServiceCallbackHandler extends Handler {
	public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();
	public final static int START_SERVICE_SUCCESS = 1;
	public final static int START_SERVICE_BLUETOOTH_NOT_ENABLE = 2;
	public final static int START_SERVICE_BLUETOOTH_OFF = 4;

	public ArrayList<BTInfo> aOnlineDeviceList;

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case GlobalSetting.MESSAGE_STATUS_BLUETOOTH_OFF:
			BlueToothDisabled();
			StartServiceStatus(false,START_SERVICE_BLUETOOTH_OFF);
			break;
		case GlobalSetting.MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE:
			BlueToothInNotEnable();
			StartServiceStatus(false,START_SERVICE_BLUETOOTH_NOT_ENABLE);
			break;
		case GlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS:
			StartServiceSuccess();
			StartServiceStatus(true,START_SERVICE_SUCCESS);
			break;
		case GlobalSetting.MESSAGE_STOP_DISCOVERY_SERVICE:
			StopService();
			break;
		case GlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND:
			Log.i(TAG, "Device not found!");
			OnlineDeviceNotFound();
			DeviceDiscoveryStatus(false,null);
			break;
		case GlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED:
			discoveryFinished();
			break;
		case GlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_LIST:
			Log.i(TAG, "Online Device found!");
			try {
				Bundle aBundle = msg.getData();
				aOnlineDeviceList = (ArrayList<BTInfo>) (aBundle.get(GlobalSetting.BUNDLE_ONLINE_DEVICE_LIST));
				if (aOnlineDeviceList != null && aOnlineDeviceList.size() > 0) {
					getOnlineDevice(aOnlineDeviceList.get(0));
					DeviceDiscoveryStatus(true,aOnlineDeviceList.get(0));
				}

			} catch (Exception ee) {
				ee.printStackTrace();
				OnlineDeviceNotFound();
				DeviceDiscoveryStatus(false,null);
			}
			break;
		}
		;
	}

	/**
	 * when user disable bluetooth.
	 * @deprecated  use ServiceStatus instead of .
	 * StartServiceStatus(false,int icode)  icode =  DiscoveryServiceCallbackHandler.START_SERVICE_BLUETOOTH_OFF
	 */
	public void BlueToothDisabled() {
		Log.i(TAG, "bluetooth disabled!");
	}

	/**
	 * when  bluetooth not enable .
	 *  @deprecated  use ServiceStatus instead of .
	 *  StartServiceStatus(false,int icode)  icode =  DiscoveryServiceCallbackHandler.START_SERVICE_BLUETOOTH_NOT_ENABLE
	 */
	public void BlueToothInNotEnable() {
		Log.i(TAG, "BLUETOOTH_NOT_ENABLE!");
	}

	/**
	 * when DiscoveryService stop
	 */
	public void StopService() {
		Log.i(TAG, "StopDisconeryService");
	}

	/**
	 * get online device bluetooth info when ClassicDiscoveryService discover and
	 * found bluetooth device.
	 * @deprecated  use DeviceDiscoveryStatus(true,BTInfo aBTInfo) instead of
	 * @param aBTInfo
	 *
	 */
	public void getOnlineDevice(BTInfo aBTInfo){};

	/**
	 * @deprecated  use DeviceDiscoveryStatus(false,null) instead of
	 *  device not found!
	 */
	public void OnlineDeviceNotFound(){};

	/**
	 * if discovery finished , trigger this method
	 */
	public void discoveryFinished(){};
	
	/**
	 * @deprecated  use ServiceStatus instead of .
	 *  StartServiceStatus(true,int icode)  icode = DiscoveryServiceCallbackHandler.START_SERVICE_SUCCESS
	 * when DiscoveryService starting
	 */
	public void StartServiceSuccess() {};

	/**
	 * next big version (6.0.0)  must set as abstract
	 * @param status
	 * @param  icode
	 */
	public void StartServiceStatus(boolean status,int icode){

	}

	/**
	 * next big version (6.0.0)  must set as abstract
	 * @param aBTInfo
	 */
	public void DeviceDiscoveryStatus(boolean status,BTInfo aBTInfo){};
}