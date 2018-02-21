package inmethod.android.bt;

public class GlobalSetting {
	public static final String TAG = "InMethod-Android-BT";
	public static final String VERSION = "6.0.1b1";

	private static boolean enable_simulation = false;
	private static byte[] byteAdvertisementData = null;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_READ_TIMEOUT = 6;
	public static final int MESSAGE_READ_FORMAT_ERROR = 7;
	public static final int MESSAGE_SEND_DATA = 8;
	public static final int MESSAGE_CONNECTION_LOST = 9;
	public static final int MESSAGE_CONNECTION_FAIL = 10;
	public static final int MESSAGE_SERVICE_STOP = 11;
	public static final int MESSAGE_CONNECTED = 12;
	public static final int MESSAGE_DELAY_SEND_BT_COMMAND = 13;
	public static final int MESSAGE_READ_BUT_NO_COMMNAND_HANDLE = 14;
	public static final int MESSAGE_DISCONNECTED = 15;

	public static final String DEVICE_NAME = "device_name";
	public static final String DEVICE_ADRESS = "device_adress";
	public static final String TOAST = "toast";

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	public static final int FOR_RESULT_OK = 1;

	// SPP UUID
	public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	// notification UUID
	public static final String Client_Characteristic_Configuration = "00002902-0000-1000-8000-00805f9b34fb";


	// for BlueToothWatcher
	public static final int MESSAGE_START_DISCOVERY_SERVICE_SUCCESS = 10001;
	public static final int MESSAGE_STOP_DISCOVERY_SERVICE = 10002;
	public static final int MESSAGE_STATUS_TRY_PAIRING = 10003;
	public static final int MESSAGE_STATUS_ONLINE_DEVICE_FOUND = 10004;
	public static final int MESSAGE_STATUS_NO_BLUETOOTH_MODULE = 10005;
	public static final int MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE = 10006;
	public static final int MESSAGE_STATUS_DEVICE_NOT_FOUND = 1007;
	public static final int MESSAGE_STATUS_DISCONNECT_REQUESTED = 10008;
	public static final int MESSAGE_STATUS_BLUETOOTH_OFF = 10009;
	public static final int MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED = 10011;

	// for DeviceConnection
	public static final int MESSAGE_EXCEPTION_NO_WRITER_UUID = 20001;
	public static final int MESSAGE_EXCEPTION_NO_READER_UUID = 20003;
	public static final int MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS = 20004;
	public static final int MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL = 20005;
	public static final int MESSAGE_RAW_DATA = 20006;
	public static final int MESSAGE_UNKNOWN_EXCEPTION = 20007;

	public static final String BUNDLE_ONLINE_DEVICE = "ONLINE_DEVICE_LIST";

	public static final String BUNDLE_KEY_DATA = "Data";
	public static final String BUNDLE_KEY_BLUETOOTH_INFO = "BTInfo";
	public static final String BUNDLE_KEY_DEVICE_TYPE = "DeviceType";
	public static final String BUNDLE_KEY_DATA_FORMAT_ERROR_STRING = "data format error";
	public static final String BUNDLE_KEY_READER_UUID_STRING = "BUNDLE_KEY_NOTIFICATION_OR_INDICATOR";
	public static final String BUNDLE_KEY_UNKNOWN_EXCEPTION_STRING = "BUNDLE_KEY_UNKNOWN_EXCEPTION_STRING";

	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
	// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
	// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
	// device
	public static final int STATE_LOST = 6; // now connected to a remote device
	/**
	 * standard service uuid is blood pressure service 0x1810.
	 */
	public static final String STANDARD_BLOOD_PRESSURE_SERVICE = "00001810-0000-1000-8000-00805f9b34fb";

	/**
	 * STANDARD_BLOOD_PRESSURE_SERVICE_BPM_UUID characteristic uuid is 0x2A35
	 */
	public static final String STANDARD_BLOOD_PRESSURE_SERVICE_BPM_UUID = "00002a35-0000-1000-8000-00805f9b34fb";

	/**
	 * STANDARD_BLOOD_PRESSURE_SERVICE_BPF_ICP_UUID characteristic uuid is
	 * 0x2A36
	 */
	public static final String STANDARD_BLOOD_PRESSURE_SERVICE_ICP_UUID = "00002a36-0000-1000-8000-00805f9b34fb";

	/**
	 * STANDARD_BLOOD_PRESSURE_SERVICE_BPF(blood pressure feature)
	 * characteristic uuid is 0x2A49
	 */
	public static final String STANDARD_BLOOD_PRESSURE_SERVICE_BPF_UUID = "00002a49-0000-1000-8000-00805f9b34fb";

	/**
	 * device information under service 0x180a
	 */
	public static final String DEVICE_INFORMATION_FIRMWARE_REVISION_UUID = "00002a26-0000-1000-8000-00805f9b34fb";


	/**
	 *   Note: only for ble device
	 *  IDiscoveryService , IChatService , DeviceConnection become simulation mode.
	 *  allway found & connecti success
	 * @param status
     */
	public static void setSimulation(boolean status){
		setSimulation(status,null);
	}

	public static void setSimulation(boolean status,byte[] advertisementData){
		enable_simulation = status;
		byteAdvertisementData = advertisementData;
	}

	public static boolean getSimulation(){
		return enable_simulation;
	}
	public static byte[] getSimulationAdvertisement(){
		return byteAdvertisementData;
	}
}
