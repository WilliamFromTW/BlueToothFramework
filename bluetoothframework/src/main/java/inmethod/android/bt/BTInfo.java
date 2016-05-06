package inmethod.android.bt;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Get bluetooth device information.
 * 
 * @author william chen
 * 
 */
public class BTInfo implements Parcelable {

	protected int iDeviceBlueToothType;
	protected String sDeviceName;
	protected String sDeviceAddress;
	protected byte[] broadcast;

	/**
	 * reference to BluetoothDevice.DEVICE_TYPE_CLASSIC
	 */
	public static final int DEVICE_TYPE_CLASSIC = BluetoothDevice.DEVICE_TYPE_CLASSIC;

	/**
	 * reference to BluetoothDevice.DEVICE_TYPE_DUAL
	 */
	public static final int DEVICE_TYPE_DUAL = BluetoothDevice.DEVICE_TYPE_DUAL;

	/**
	 * reference to BluetoothDevice.DEVICE_TYPE_LE
	 */
	public static final int DEVICE_TYPE_LE = BluetoothDevice.DEVICE_TYPE_LE;

	/**
	 * reference to BluetoothDevice.DEVICE_TYPE_UNKNOWN
	 */
	public static final int DEVICE_TYPE_UNKNOWN = BluetoothDevice.DEVICE_TYPE_UNKNOWN;

	/**
	 * bluetooth type.
	 * 
	 * <pre>
	 * DEVICE_TYPE_CLASSIC
	 * DEVICE_TYPE_DUAL
	 * DEVICE_TYPE_LE
	 * DEVICE_TYPE_UNKNOWN
	 * </pre>
	 * 
	 * @param iBluetoothType
	 */
	public void setDeviceBlueToothType(int iBluetoothType) {
		iDeviceBlueToothType = iBluetoothType;
	}

	/**
	 * please reference android.bluetooth.BluetoothDevice.
	 * 
	 * <pre>
	 * DEVICE_TYPE_CLASSIC
	 * DEVICE_TYPE_DUAL
	 * DEVICE_TYPE_LE
	 * DEVICE_TYPE_UNKNOWN
	 * </pre>
	 * 
	 * @see android.bluetooth.BluetoothDevice
	 * @return
	 */
	public int getDeviceBlueToothType() {
		return iDeviceBlueToothType;
	}

	/**
	 * get bluetooth device name
	 * 
	 * @return
	 */
	public String getDeviceName() {
		return sDeviceName;
	}

	/**
	 * get bluetooth device MAC address
	 * 
	 * @return
	 */
	public String getDeviceAddress() {
		return sDeviceAddress;
	}

	/**
	 * set bluetooth device Name
	 * 
	 * @param sDeviceName
	 */
	public void setDeviceName(String sDeviceName) {
		this.sDeviceName = sDeviceName;
	}

	/**
	 * set bluetooth device MAC address name
	 * 
	 * @param sDeviceAddress
	 */
	public void setDeviceAddress(String sDeviceAddress) {
		this.sDeviceAddress = sDeviceAddress;
	}
	
	/**
	 * ble broadcase
	 */
	public void setBroadcastData(byte[] broadcast){
		this.broadcast = broadcast;
	}

	/**
	 * 
	 */
	public byte[] getBroadcastData(){
		return broadcast;
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeString(sDeviceName);
		arg0.writeString(sDeviceAddress);
		arg0.writeInt(iDeviceBlueToothType);
		if( broadcast!=null && broadcast.length>0)
		  arg0.writeByteArray(broadcast);
		else
		  arg0.writeByteArray(new byte[]{0});
		
	}

	public void readFromParcel(Parcel in) {
		sDeviceName = in.readString();
		sDeviceAddress = in.readString();
		iDeviceBlueToothType = in.readInt();
		in.readByteArray(broadcast);
	}

	public static final Parcelable.Creator<BTInfo> CREATOR = new Parcelable.Creator<BTInfo>() {
		public BTInfo createFromParcel(Parcel in) {
			BTInfo aBTInfo = new BTInfo();
			aBTInfo.readFromParcel(in);
			return aBTInfo;
		}

		public BTInfo[] newArray(int size) {
			return new BTInfo[size];
		}
	};


}
