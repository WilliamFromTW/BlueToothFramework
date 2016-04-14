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
			RawData(aInfo, (byte) msg.arg1, aBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING));

			break;
		case BlueToothGlobalSetting.MESSAGE_UNKNOWN_EXCEPTION:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DeviceUnknownException(aInfo,aBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_UNKNOWN_EXCEPTION_STRING));
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
	public abstract void NoReaderUUIDException(BTInfo aBTInfo) ;


	/**
	 * command can't send to device.
	 * 
	 * @param aBTInfo
	 */
	public abstract void NoWriterUUIDException(BTInfo aBTInfo);

	/**
	 * data that does not handled by commands.
	 * 
	 * @param aBTInfo
	 */
	public void DataNotHandled(BTInfo aBTInfo, byte rawData, String sUUID) {
	};

	/**
	 * all raw data .
	 * 
	 * @param aBTInfo
	 */
	public void RawData(BTInfo aBTInfo, byte rawData, String sUUID) {
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

	public abstract void DeviceUnknownException(BTInfo aBTInfo,String sException);
	
	public abstract void DeviceNotificationOrIndicatorEnableSuccess(BTInfo aBTInfo,String sUUID);
	
	public abstract void DeviceNotificationOrIndicatorEnableFail(BTInfo aBTInfo,String sUUID);
}
