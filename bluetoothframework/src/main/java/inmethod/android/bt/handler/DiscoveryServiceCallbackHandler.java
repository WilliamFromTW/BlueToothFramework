package inmethod.android.bt.handler;

import android.bluetooth.le.ScanCallback;
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
    public final static int START_SERVICE_BLUETOOTH_ON = 8;
    public final static int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED;

    public BTInfo aOnlineDevice;

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GlobalSetting.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                StartServiceStatus(false, SCAN_FAILED_APPLICATION_REGISTRATION_FAILED);
                break;
            case GlobalSetting.MESSAGE_STATUS_BLUETOOTH_OFF:
                StartServiceStatus(false, START_SERVICE_BLUETOOTH_OFF);
                break;
            case GlobalSetting.MESSAGE_STATUS_BLUETOOTH_ON:
                StartServiceStatus(false, START_SERVICE_BLUETOOTH_ON);
                break;
            case GlobalSetting.MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE:
                StartServiceStatus(false, START_SERVICE_BLUETOOTH_NOT_ENABLE);
                break;
            case GlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS:
                StartServiceStatus(true, START_SERVICE_SUCCESS);
                break;
            case GlobalSetting.MESSAGE_STOP_DISCOVERY_SERVICE:
                StopService();
                break;
            case GlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND:
                //Log.i(TAG, "Device not found!");
                DeviceDiscoveryStatus(false, null);
                break;
            case GlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED:
                discoveryFinished();
                break;
            case GlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_FOUND:
                //Log.i(TAG, "Online Device found!");
                try {
                    Bundle aBundle = msg.getData();
                    aOnlineDevice = (BTInfo) (aBundle.get(GlobalSetting.BUNDLE_ONLINE_DEVICE));

                    DeviceDiscoveryStatus(true, aOnlineDevice);

                } catch (Exception ee) {
                    ee.printStackTrace();
                    DeviceDiscoveryStatus(false, null);
                }
                break;
        }
        ;
    }

    /**
     * when DiscoveryService stop
     */
    public void StopService() {
        Log.i(TAG, "StopDisconeryService");
    }


    /**
     * if discovery finished , trigger this method
     */
    public void discoveryFinished() {
    }

    ;

    /**
     *  check Start Service result status
     *
     * @param status
     * @param icode  DiscoveryServiceCallbackHandler.MESSAGE_STATUS_BLUETOOTH_OFF , DiscoveryServiceCallbackHandler..MESSAGE_STATUS_BLUETOOTH_ON,  DiscoveryServiceCallbackHandler.START_SERVICE_BLUETOOTH_NOT_ENABLE,  DiscoveryServiceCallbackHandler.START_SERVICE_SUCCESS
     */
    public abstract void StartServiceStatus(boolean status, int icode) ;

    /**
     * next big version (6.0.0)  must set as abstract
     *
     * @param aBTInfo
     */
    public abstract void DeviceDiscoveryStatus(boolean status, BTInfo aBTInfo);

}