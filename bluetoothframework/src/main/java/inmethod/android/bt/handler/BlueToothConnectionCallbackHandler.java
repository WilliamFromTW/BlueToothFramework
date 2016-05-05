package inmethod.android.bt.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.BlueToothGlobalSetting;
import inmethod.android.bt.command.BTCommands;

public abstract class BlueToothConnectionCallbackHandler extends Handler {
	public final String TAG = BlueToothGlobalSetting.TAG + "/" + getClass().getSimpleName();
	Bundle aBundle = null;
	BTInfo aInfo = null;
	byte[] byteData = null;

	public void handleMessage(Message msg) {
		switch (msg.what) {

		case BlueToothGlobalSetting.MESSAGE_CONNECTED:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DeviceConnected(aInfo);
			break;
		case BlueToothGlobalSetting.MESSAGE_CONNECTION_FAIL:
		case BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DeviceConnectionLost(aInfo);
			break;
		case BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_READER_UUID:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			NoReaderUUIDException(aInfo);
			break;
		case BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			NoWriterUUIDException(aInfo);
			break;
		case BlueToothGlobalSetting.MESSAGE_READ_BUT_NO_COMMNAND_HANDLE: // data that
																	// does not
																	// handled
																	// by
																	// commands
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DataNotHandled(aInfo, (byte) msg.arg1, aBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			break;

		case BlueToothGlobalSetting.MESSAGE_RAW_DATA: // all raw data
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			responseNotificationData(aInfo, (byte) msg.arg1, aBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING));

			break;
		case BlueToothGlobalSetting.MESSAGE_UNKNOWN_EXCEPTION:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			UnknownException(aInfo,aBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_UNKNOWN_EXCEPTION_STRING));
			break;
		case BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DeviceNotificationOrIndicatorEnableSuccess(aInfo,aBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			break;
		case BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DeviceNotificationOrIndicatorEnableFail(aInfo,aBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			break;
		}
	}

	/**
	 * command can't send to device.
	 * 
	 * @param aBTInfo
	 */
	public void NoReaderUUIDException(BTInfo aBTInfo){};


	/**
	 * command can't send to device.
	 * 
	 * @param aBTInfo
	 */
	public void NoWriterUUIDException(BTInfo aBTInfo){};

	/**
	 * data that does not handled by commands.
	 * 
	 * @param aBTInfo
	 */
	public void DataNotHandled(BTInfo aBTInfo, byte rawData, String sUUID) {
	};

	/**
	 * all responseNotification or indicator data
	 * 
	 * @param aBTInfo
	 * @param rawData byte data
	 * @param sUUID notification or indicator UUID
	 */
	public void responseNotificationData(BTInfo aBTInfo, byte rawData, String sUUID) {
	};

	/**
	 * Device is connected!
	 * 
	 * @param aBTInfo
	 */
	public abstract void DeviceConnected(BTInfo aBTInfo);

	/**
	 * Device connection lost!
	 * 
	 * @param aBTInfo
	 */
	public abstract void DeviceConnectionLost(BTInfo aBTInfo);

	/**
	 * handle unknown exception
	 * @param aBTInfo
	 * @param sException
     */
	public void UnknownException(BTInfo aBTInfo,String sException){};

	/**
	 * If BLE device set notification or indicator success , than trigger this method
	 * @param aBTInfo
	 * @param sUUID
     */
	public void DeviceNotificationOrIndicatorEnableSuccess(BTInfo aBTInfo,String sUUID){};

	/**
	 * If BLE device set notification or indicator fail , than trigger this method
	 * @param aBTInfo
	 * @param sUUID
     */
	public void DeviceNotificationOrIndicatorEnableFail(BTInfo aBTInfo,String sUUID){};
}
