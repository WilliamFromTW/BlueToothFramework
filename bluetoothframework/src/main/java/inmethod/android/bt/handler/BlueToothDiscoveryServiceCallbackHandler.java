package inmethod.android.bt.handler;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.BlueToothGlobalSetting;

public abstract class BlueToothDiscoveryServiceCallbackHandler extends Handler {
	public final String TAG = BlueToothGlobalSetting.TAG + "/" + getClass().getSimpleName();

	public static ArrayList<BTInfo> aOnlineDeviceList;

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case BlueToothGlobalSetting.MESSAGE_STATUS_BLUETOOTH_OFF:
			BlueToothDisabled();
			break;
		case BlueToothGlobalSetting.MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE:
			BlueToothInNotEnable();
			break;
		case BlueToothGlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS:
			StartDiscoveryServiceSuccess();
			break;
		case BlueToothGlobalSetting.MESSAGE_STOP_DISCOVERY_SERVICE:
			StopDiscoveryService();
			break;
		case BlueToothGlobalSetting.MESSAGE_STATUS_TRY_PAIRING:
			TryToPairing();
			break;
		case BlueToothGlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND:
			Log.i(TAG, "Device not found!");
			OnlineDeviceNotFound();
			break;
		case BlueToothGlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED:
			discoveryFinished();
			break;
		case BlueToothGlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_LIST:
			Log.i(TAG, "Online Device found!");
			try {
				Bundle aBundle = msg.getData();
				aOnlineDeviceList = (ArrayList<BTInfo>) (aBundle.get(BlueToothGlobalSetting.BUNDLE_ONLINE_DEVICE_LIST));
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
	 * when bluetooth is trying to pair
	 */
	public void TryToPairing() {
		Log.i(TAG, "TRY_PAIRING...");
	}


	/**
	 * when DiscoveryService stop
	 */
	public void StopDiscoveryService() {
		Log.i(TAG, "StopDisconeryService");
	}

	/**
	 * get online device bluetooth info when BlueToothDiscoveryService discover and
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
	public abstract void StartDiscoveryServiceSuccess() ;

}