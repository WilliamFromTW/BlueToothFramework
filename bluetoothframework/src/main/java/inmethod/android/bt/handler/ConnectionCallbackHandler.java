package inmethod.android.bt.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.GlobalSetting;

public abstract class ConnectionCallbackHandler extends Handler {
	public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();
	Bundle aBundle = null;
	BTInfo aInfo = null;
	byte[] byteData = null;

	public void handleMessage(Message msg) {
		switch (msg.what) {

		case GlobalSetting.MESSAGE_CONNECTED:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DeviceConnected(aInfo);
			DeviceConnectionStatus(true,aInfo);
			break;
		case GlobalSetting.MESSAGE_CONNECTION_FAIL:
		case GlobalSetting.MESSAGE_CONNECTION_LOST:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DeviceConnectionLost(aInfo);
			DeviceConnectionStatus(false,aInfo);
			break;
		case GlobalSetting.MESSAGE_EXCEPTION_NO_READER_UUID:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			NoReaderUUIDException(aInfo);
			break;
		case GlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			NoWriterUUIDException(aInfo);
			break;
		case GlobalSetting.MESSAGE_READ_BUT_NO_COMMNAND_HANDLE: // data that
																	// does not
																	// handled
																	// by
																	// commands
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			DataNotHandled(aInfo, (byte) msg.arg1, aBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			break;

		case GlobalSetting.MESSAGE_RAW_DATA: // all raw data
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			responsedData(aInfo, (byte) msg.arg1, aBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));

			break;
		case GlobalSetting.MESSAGE_UNKNOWN_EXCEPTION:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			UnknownException(aInfo,aBundle.getString(GlobalSetting.BUNDLE_KEY_UNKNOWN_EXCEPTION_STRING));
			break;
		case GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			NotificationEnabled(aInfo,aBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			NotificationStatus(true,aInfo,aBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			break;
		case GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL:
			aBundle = msg.getData();
			aInfo = aBundle.getParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO);
			NotificationEnableFail(aInfo,aBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			NotificationStatus(false,aInfo,aBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
			break;
			default:
				handleExtraMessage(msg);
				break;
		}
	}

	/**
	 * handler extra message
	 *
	 * @param msg
	 */
	public  void handleExtraMessage(Message msg){}

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
	 * All responsed data from Notification or indicator characteristic
	 *
	 * @param aBTInfo
	 * @param rawData byte data
	 * @param sUUID notification or indicator UUID
	 */
	public void responsedData(BTInfo aBTInfo, byte rawData, String sUUID) {
	};


	/**
	 * Device is connected!
	 * @deprecated  use DeviceConnectionStatus(true,BTInfo aBTInfo) instead of
	 * @param aBTInfo
	 */
	public void DeviceConnected(BTInfo aBTInfo){};

	/**
	 * Device connection lost!
	 * @deprecated  use DeviceConnectionStatus(false,BTInfo aBTInfo) instead of
	 * @param aBTInfo
	 */
	public void DeviceConnectionLost(BTInfo aBTInfo){};

	public void DeviceConnectionStatus(boolean status,BTInfo aBTInfo){};

	/**
	 * handle unknown exception
	 * @param aBTInfo
	 * @param sException
     */
	public void UnknownException(BTInfo aBTInfo,String sException){};

	/**
	 * If BLE device set notification or indicator success , than trigger this method
	 * @deprecated  use NotificationStatus(true,BTInfo aBTInfo, String sUUID) instead of.
	 * @param aBTInfo
	 * @param sUUID
     */
	public void NotificationEnabled(BTInfo aBTInfo, String sUUID){};

	/**
	 * If BLE device set notification or indicator fail , than trigger this method
	 * @deprecated  use NotificationStatus(false,BTInfo aBTInfo, String sUUID) instead of.
	 * @param aBTInfo
	 * @param sUUID
     */
	public void NotificationEnableFail(BTInfo aBTInfo, String sUUID){};


	/**
	 * next big version (6.0.0) must set abstract
	 * @param Status
	 * @param aBTInfo
	 * @param sUUID
	 */
	public void NotificationStatus(boolean Status,BTInfo aBTInfo, String sUUID){

	}
}
