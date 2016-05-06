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

	public ArrayList<BTInfo> aOnlineDeviceList;

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case GlobalSetting.MESSAGE_STATUS_BLUETOOTH_OFF:
			BlueToothDisabled();
			break;
		case GlobalSetting.MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE:
			BlueToothInNotEnable();
			break;
		case GlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS:
			StartServiceSuccess();
			break;
		case GlobalSetting.MESSAGE_STOP_DISCOVERY_SERVICE:
			StopService();
			break;
		case GlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND:
			Log.i(TAG, "Device not found!");
			OnlineDeviceNotFound();
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
				}

			} catch (Exception ee) {
				ee.printStackTrace();
				OnlineDeviceNotFound();
			}
			break;
		}
		;
	}

	/**
	 * when user disable bluetooth.
	 */
	public void BlueToothDisabled() {
		Log.i(TAG, "bluetooth disabled!");
	}

	/**
	 * when user disable bluetooth.
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
	 * 
	 * @param aBTInfo
	 *
	 */
	public abstract void getOnlineDevice(BTInfo aBTInfo);

	/**
	 * device not found!
	 */
	public abstract void OnlineDeviceNotFound();

	/**
	 * if discovery finished , trigger this method
	 */
	public void discoveryFinished(){};
	
	/**
	 * when DiscoveryService starting
	 */
	public abstract void StartServiceSuccess() ;

}