package inmethod.android.bt.le;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import inmethod.android.bt.BTInfo;
import inmethod.android.bt.GlobalSetting;
import inmethod.android.bt.handler.DiscoveryServiceCallbackHandler;
import inmethod.android.bt.interfaces.IDiscoveryService;

public class LeDiscoveryService implements IDiscoveryService {
    // Debugging
    public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();
    protected Context aContext = null;
    private DiscoveryServiceCallbackHandler mHandler;
    private Handler stopScanHandler;
    protected boolean bRun = false;
    protected BluetoothAdapter mBluetoothAdapter = null;
    private android.bluetooth.le.ScanCallback mLeScanCallback = null;
    // bluetooth set
    protected ArrayList<BTInfo> aOnlineDeviceList = new ArrayList<BTInfo>();
    protected BTInfo aOnlineDevice = null;
    protected BTInfo aBTInfo = null;

    private boolean alwaysCallBackIfTheSameDeviceDiscovery = false;
    private static int iScanTimeoutMilliseconds = 6000;

    protected boolean bDeviceFound = false;
    private static LeDiscoveryService aLeDiscoveryService = null;

    private boolean isDiscovering = false;
    private boolean bCancelDiscovery = false;
    private Vector<String> aFilter = null;

    private int iDefaultDiscoveryMode = DISCOVERY_MODE_FOUND_AND_CONTINUE_DISCOVERY;

    private LeDiscoveryService() {
    }

    /**
     * Discovery timeout default is 12000.
     * @param iMilliseconds
     */
    public void setScanTimeout(int iMilliseconds){
       iScanTimeoutMilliseconds = iMilliseconds;
    }

    /**
     * get discovery timeout milliseconds , default is 12000.
     * @return
     */
    public int getScanTimeout(){
      return iScanTimeoutMilliseconds;
    }

    @Override
    public void alwaysCallBackIfTheSameDeviceDiscovery(boolean bAlways) {
        alwaysCallBackIfTheSameDeviceDiscovery = bAlways;
    }

    ;

    /**
     * Singleton Class
     *
     * @return
     */
    public static LeDiscoveryService getInstance() {
        if (aLeDiscoveryService == null) {
            aLeDiscoveryService = new LeDiscoveryService();
            aLeDiscoveryService.stopScanHandler = new Handler();
        }
        return aLeDiscoveryService;
    }

    /**
     * Context , must not be null
     *
     * @param aC context or activity or application context(service)
     */
    public void setContext(Context aC) {
        aContext = aC;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * call back message handler , must not be null
     *
     * @param mHandler
     */
    public void setCallBackHandler(DiscoveryServiceCallbackHandler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public DiscoveryServiceCallbackHandler getCallBackHandler() {
        return mHandler;
    }

    /**
     * check bluetooth device is enable or disable.
     *
     * @return
     */
    public boolean isBlueToothReady() {
        if (mBluetoothAdapter == null) {
            try {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        if (mBluetoothAdapter == null)
            return false;
        else
            return true;
    }

    public boolean prepareBluetoothAdapter() {
        // Get local Bluetooth adapter
        Log.d(TAG, "prepareBluetoothAdapter");
        if( mBluetoothAdapter !=null ){
            mBluetoothAdapter = null;
        }
        try{
            Thread.sleep(100);
        }catch (Exception ee){

        }
        if (mBluetoothAdapter == null) {
            try {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                Log.d(TAG, "prepareBLEAdapter");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_NO_BLUETOOTH_MODULE).sendToTarget();
            return false;
        }
        try {
            Log.d(TAG, "for safety reason , unregisterReceiver before registerReceiver!");
            aContext.unregisterReceiver(mReceiver);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        IntentFilter state_change_filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        try {
            aContext.registerReceiver(mReceiver, state_change_filter);
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        mLeScanCallback = null;
        mLeScanCallback = new android.bluetooth.le.ScanCallback() {

            @Override
            public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                if ( !isRunning()||bCancelDiscovery)
                    return;
                BluetoothDevice device = result.getDevice();
                if (filterFoundBTDevice(device.getName()) || filterFoundBTDevice(device.getAddress())) {

                    boolean bTheSameDeviceFound = false;
                    for (BTInfo aInfo : aOnlineDeviceList) {
                        if (aInfo.getDeviceAddress().equalsIgnoreCase(device.getAddress()) ) {
                            bTheSameDeviceFound = true;
                        }
                    }
                    if( alwaysCallBackIfTheSameDeviceDiscovery == false && bTheSameDeviceFound) return;

                    else {
                        BTInfo aBTInfo = new BTInfo();
                        aBTInfo.setDeviceAddress(device.getAddress());
                        aBTInfo.setDeviceName(device.getName());
                        aBTInfo.setDeviceBlueToothType( device.getType() );
                        aBTInfo.setRSSI(result.getRssi());
                        aBTInfo.setAdvertisementData(result.getScanRecord().getBytes());
                        if( !bTheSameDeviceFound )  aOnlineDeviceList.add(aBTInfo);
                         Log.i(TAG,"name="+aBTInfo.getDeviceName()+",address="+aBTInfo.getDeviceAddress()+",type="+aBTInfo.getDeviceBlueToothType() );
                        bDeviceFound = true;
                        Message msg = mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_FOUND);
                        Bundle bundle = new Bundle();
                        aOnlineDevice = aBTInfo;
                        bundle.putParcelable (GlobalSetting.BUNDLE_ONLINE_DEVICE, aOnlineDevice);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);

                        if (iDefaultDiscoveryMode == LeDiscoveryService.DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY) {
                            cancelDiscovery();
                            mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED).sendToTarget();
                        }
                    }
                }
            }
            @Override
            public void onScanFailed(int errorCode) {

                Log.i(TAG, "error code is:" + errorCode);
                if( errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                   Log.e(TAG,"Scan Status  = ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED , Please consider disable bluetooth and enable bluetooth power");
                   mHandler.obtainMessage(GlobalSetting.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED).sendToTarget();
                }
            };

        };


        return true;
    }

    /**
     * check this DiscoveryService is running
     *
     * @return true : running , false not running
     */
    public boolean isRunning() {
        return bRun;
    }

    /**
     * stop service
     */
    public void stopService() {
        Log.d(TAG, "stopDiscoveryService()");
        clearData();
        mHandler.obtainMessage(GlobalSetting.MESSAGE_STOP_DISCOVERY_SERVICE).sendToTarget();
        bRun = false;
    }

    /**
     * stop service
     */
    public void stopServiceWithoutNotify() {
        Log.d(TAG, "stopServiceWithoutNotify()");
        clearData();
        bRun = false;
    }

    /**
     * start service , if bluetooth is not enable , LeDiscoveryService will
     * stop if context or handler is null , LeDiscoveryService will stop
     */
    public void startService() throws Exception {

        if (aContext == null || this.mHandler == null)
            throw new Exception("context or handler cannot be null");
        if (isRunning())
            return;
        aOnlineDeviceList.clear();

        Log.d(TAG, "startDiscoveryService()");
        bRun = true;
        bCancelDiscovery = false;
        if( GlobalSetting.getSimulation()){
            mHandler.obtainMessage(GlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS).sendToTarget();
            return;
        }
        if (!prepareBluetoothAdapter()) {
            this.stopService();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                clearData();
                mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_BLUETOOTH_NOT_ENABLE).sendToTarget();
            } else {
                mHandler.obtainMessage(GlobalSetting.MESSAGE_START_DISCOVERY_SERVICE_SUCCESS).sendToTarget();
            }
        }
    }

    /**
     * clear variable
     */
    protected void clearData() {
        bRun = false;
        bDeviceFound = false;
        if (aOnlineDeviceList != null)
            aOnlineDeviceList.clear();
        if (mBluetoothAdapter.isDiscovering() & !GlobalSetting.getSimulation())
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        isDiscovering = false;
        bCancelDiscovery = true;
        if( GlobalSetting.getSimulation()){
            return;
        }
        try {
            Log.d(TAG, "for safety reason , unregisterReceiver before registerReceiver!");
            aContext.unregisterReceiver(mReceiver);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

    }

    /**
     * check this class is doing discovery
     *
     * @return
     */
    public boolean isDiscovering() {
        return isDiscovering;
    }

    public void cancelDiscovery() {
        if (GlobalSetting.getSimulation()) {
            isDiscovering = false;
            return;
        }

        if (!isRunning()) return;
        if (mBluetoothAdapter != null) {
            bCancelDiscovery = true;
            mHandler.removeMessages(GlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND);
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if( mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
                Log.d(TAG, "cancelDiscovery()");

                isDiscovering = false;
            }
        }, 10);

    }

    public void doDiscovery() {
       doDiscovery(iScanTimeoutMilliseconds) ;
    }

    /**
     * discovery manually
     */
    public void doDiscovery(int iScanTimeoutMilliseconds) {
        if (mBluetoothAdapter != null) {
            if (isDiscovering())  return;
        }
        bDeviceFound = false;
        if (GlobalSetting.getSimulation()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BTInfo aBTInfo = new BTInfo();
                    aBTInfo.setDeviceAddress("A1:A2:A3:A4:A5:A6");
                    aBTInfo.setDeviceName("BleSimulation");
                    aBTInfo.setDeviceBlueToothType(BTInfo.DEVICE_TYPE_LE);
                    aBTInfo.setAdvertisementData(GlobalSetting.getSimulationAdvertisement());
                    boolean bFound = true;
                    aOnlineDeviceList.add(aBTInfo);
                    bDeviceFound = true;
                    Message msg = mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_ONLINE_DEVICE_FOUND);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(GlobalSetting.BUNDLE_ONLINE_DEVICE,  aOnlineDevice);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED).sendToTarget();
                    aOnlineDeviceList.clear();
                    aOnlineDevice = null;
                }
            }, 1000);
            return;
        }
        aOnlineDeviceList.clear();

        Log.d(TAG, "is running?" + isRunning()+",is discovering?"+ mBluetoothAdapter.isDiscovering() );
        if (!isRunning()) return;
        Log.d(TAG, "doDiscovery()");

        try {
            final List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder().build();
            filters.add(filter);

            final ScanSettings scanSettings =
                    new ScanSettings.Builder() .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT).setMatchMode(ScanSettings.MATCH_MODE_STICKY).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            if( !mBluetoothAdapter.isDiscovering() ) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.getBluetoothLeScanner().startScan(filters,scanSettings,mLeScanCallback);
                        isDiscovering = true;
                    }
                }, 10);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDiscovering = false;
                        if (mBluetoothAdapter.isDiscovering())
                            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
                        if (!isRunning()) return;
                        Log.i(TAG, "device found?" + bDeviceFound);

                        if (!bDeviceFound) {
                            Message msg = mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_DEVICE_NOT_FOUND);
                            mHandler.sendMessageDelayed(msg, 10);
                        }
                        Message msg = mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_DEVICE_DISCOVERY_FINISHED);
                        mHandler.sendMessageDelayed(msg, 100);
                    }
                }, iScanTimeoutMilliseconds);
            }

        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    /**
     * set bluetooth deivce name , DiscoveryService will avoid to get bluetooth
     * device that are not in filter list. Name is case-sensitive
     *
     * @param aVector
     */
    public void setBlueToothDeviceNameFilter(Vector<String> aVector) {
        aFilter = aVector;
    }

    /**
     * Find which bluetooth device we need to connect.
     * String maybe Device name or Mac Address(Mac Address will remove ":" character.
     *
     * @param sBTName
     * @return
     */
    private boolean filterFoundBTDevice(String sBTName) {
        if (sBTName == null) return false;
        String sLocalBTName = sBTName.replace(":", "");

        if (sLocalBTName != null) {
            if (aFilter != null) {
                for (String sName : aFilter) {
                    if (sLocalBTName.toUpperCase().indexOf(sName.toUpperCase()) != -1)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY(default), IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
     */
    @Override
    public void setDiscoveryMode(int iMode) {
        iDefaultDiscoveryMode = iMode;
    }

    /**
     * @return IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY(default), IDiscoveryService.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY
     */
    @Override
    public int getDiscoveryMode() {
        return iDefaultDiscoveryMode;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "Bluetooth Turn off!");
                        mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_BLUETOOTH_OFF).sendToTarget();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "Bluetooth Turn on!");
                        mHandler.obtainMessage(GlobalSetting.MESSAGE_STATUS_BLUETOOTH_ON).sendToTarget();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };


}