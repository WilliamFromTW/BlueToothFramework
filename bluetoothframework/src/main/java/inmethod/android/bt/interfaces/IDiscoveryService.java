package inmethod.android.bt.interfaces;

import java.util.Vector;

import android.content.Context;

import inmethod.android.bt.handler.DiscoveryServiceCallbackHandler;

public interface IDiscoveryService {

	public final static int DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY = 1001;

	public final static int DISCOVERY_MODE_FOUND_AND_CONTINUE_DISCOVERY = 1002;

	/**
	 * Context , must not be null
	 * 
	 * @param aC
	 *            context or activity or application context(service)
	 */
	public void setContext(Context aC);

	/**
	 * call back message handler , must not be null
	 * 
	 * @param mHandler
	 */
	public void setCallBackHandler(DiscoveryServiceCallbackHandler mHandler);

	/**
	 * get DiscoveryServiceCallbackHandler call back handler.
	 */
	public DiscoveryServiceCallbackHandler getCallBackHandler() ;

	/**
	 * check bluetooth device is enable or disable.
	 * 
	 * @return
	 */
	public boolean isBlueToothReady();

	
	/**
	 * check this communication is running
	 * 
	 * @return true : running , false not running
	 */
	public boolean isRunning();

	/**
	 * stop Service
	 */
	public void stopService();

	/**
	 * start service , if bluetooth is not enable , service will
	 * stop if context or handler is null , service will stop
	 */
	public void startService() throws Exception;

	/***
	 * default is
	 * BlueToothCommunication.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 * 
	 * @param iMode
	 *            one of IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 *            ,IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 */
	public void setDiscoveryMode(int iMode);

	/**
	 * get discovery mode
	 * 
	 * @return one of
	 *         IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 *         ,IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
	 */
	public int getDiscoveryMode();

	/**
	 * check this class is doing discovery
	 * 
	 * @return
	 */
	public boolean isDiscovering();

	public void cancelDiscovery();

	/**
	 * discovery manually
	 */
	public void doDiscovery();

	/**
	 * discovery manually
	 */
	public void doDiscovery(int iScanTimeout);

	/**
	 * set bluetooth deivce name , communication will avoid to get bluetooth
	 * device that are not in filter list. Name is case-sensitive
	 * 
	 * @param aVector
	 */
	public void setBlueToothDeviceNameFilter(Vector<String> aVector);

	/**
	 * for ble only.
	 * @param iMilliseconds
     */
	public void setScanTimeout(int iMilliseconds);

	/**
	 *  for ble only discovery finish
	 * @return
     */
	public int getScanTimeout();

	/**
	 *   default false only call back once if the same device found!
	 * @param bAlways
	 */
	public void alwaysCallBackIfTheSameDeviceDiscovery(boolean bAlways);


	/**
	 * prepareBluetoothAdapter
	 */
	public boolean prepareBluetoothAdapter();
}
