package inmethod.android.bt.le;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import inmethod.android.bt.GlobalSetting;
import inmethod.android.bt.exception.NoBTReaderException;
import inmethod.android.bt.exception.NoWriterException;
import inmethod.android.bt.interfaces.IChatService;

/**
 *
 */
public class LeChatService implements IChatService {
    // Debugging
    public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();
    private static final boolean D = true;

    // Member fields
    private Handler mHandler;
    private Context context;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; // we're doing nothing

    public final static String ACTION_GATT_CONNECTED = "inmethod.android.bt.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "inmethod.android.bt.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "inmethod.android.bt.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "inmethod.android.bt.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "inmethod.android.bt.EXTRA_DATA";

    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_CONNECTING = IChatService.STATE_CONNECTING;
    public static final int STATE_CONNECTED = IChatService.STATE_CONNECTED;
    public static final int STATE_GATT_SERVICES_DISCOVERED = 2001;
    public static final int STATE_LOST = IChatService.STATE_LOST;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothGattCallback mGattCallback = null;
    private ArrayList<String> aSetNotifyOrIndicatorCharacteristicListenerUUID = null;
    private HashMap<String, BluetoothGattCharacteristic> allCharacteristic = null;
    private boolean bIsWrite = false;
    private Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();
    private boolean bSimulationBluetoothGattObject = true;
    private String sSimulationResponsedUUID = null;

    private static int iNotifyOrIndicatorDelayMilliseconds = 200;

    private class NotifyOrIndicatorDelayRunnable implements Runnable {
        private BluetoothGattCharacteristic temp;
        public void run() {
            if (isCharacterisiticNotifiable(temp)) {
            setCharacteristicNotification(temp, true);
        }
            if (isCharacterisiticIndicator(temp))
                setCharacteristicIndicator(temp, true);
        }

        public NotifyOrIndicatorDelayRunnable(BluetoothGattCharacteristic a){
            temp = a;
        }

    }

    /**
     * @param adapter
     * @param context
     * @param aSetNotifyOrIndicatorCharacteristicListenerUUID these characteristic is used to enable nofity or indicator
     */
    public LeChatService(BluetoothAdapter adapter, Context context,
                         ArrayList<String> aSetNotifyOrIndicatorCharacteristicListenerUUID) {
        mBluetoothAdapter = adapter;
        mState = STATE_NONE;
        this.context = context;
        this.aSetNotifyOrIndicatorCharacteristicListenerUUID = aSetNotifyOrIndicatorCharacteristicListenerUUID;
    }

    private LeChatService() {
    }

    ;

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    public synchronized void setState(int state) {
        // Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(GlobalSetting.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    private void initial() {
        Log.d(TAG, "LeChatService initial");
    }

    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {

            for (byte bytes : data)
                mHandler.obtainMessage(GlobalSetting.MESSAGE_READ, bytes, -1, characteristic.getUuid().toString())
                        .sendToTarget();
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "LeChatService start");
        initial();
    }

    /**
     * Stop all threads
     */
    public void stop() {

        Log.d(TAG, "LeChatService synchronized stop");
        setState(STATE_NONE);
        if (GlobalSetting.getSimulation() && bSimulationBluetoothGattObject) {
            mBluetoothGatt = null;
            bSimulationBluetoothGattObject = false;
            mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST).sendToTarget();
            return;
        }
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST).sendToTarget();
        }
    }

    /**
     * @param address bluetooth mac address
     */
    public void connect(String address) {
        try {
            setState(STATE_CONNECTING);
            if (GlobalSetting.getSimulation()) {
                setState(STATE_CONNECTED);
                if( aSetNotifyOrIndicatorCharacteristicListenerUUID!=null) {
                    Log.d(TAG,"aSetNotifyOrIndicatorCharacteristicListenerUUID size ="+ aSetNotifyOrIndicatorCharacteristicListenerUUID.size());
                    for (String sUUID : aSetNotifyOrIndicatorCharacteristicListenerUUID) {
                        Log.d(TAG, "aSetNotifyOrIndicatorCharacteristicListenerUUID , uuid=" + sUUID);
                        Message aMessage = mHandler
                                .obtainMessage(GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS, 1, -1);
                        Bundle aBundle = new Bundle();
                        aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING, sUUID);
                        aMessage.setData(aBundle);
                        mHandler.sendMessage(aMessage);
                    }
                }
                return;
            }
            if (mBluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            }
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
            }

            mGattCallback = getGattCallback();
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
            Log.d(TAG, "Trying to create a new gatt connection.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              mBluetoothGatt = device.connectGatt(context, false, mGattCallback,BluetoothDevice.TRANSPORT_LE);
            }else {
              mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
            }

        } catch (Exception ex) {
            Log.e(TAG, "device.connectGatt failed!");
            mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_FAIL).sendToTarget();
            setState(STATE_LOST);
        }
    }

    /**
     * Write data to remote device .
     *
     * @param aWriterUUIDString aWriterUUIDString is UUID string that used to get
     *                          BluetoothGattCharacteristic writer object.
     * @param out
     */
    public void write(byte[] out, Object aWriterUUIDString) throws NoWriterException {
        //	Log.d(TAG, "write(byte[] out, Object aWriterUUIDString), aWriterUUIDString= "+aWriterUUIDString+",out="+HexAndStringConverter.convertHexByteToHexString(out) );
        if (!GlobalSetting.getSimulation()) {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.e(TAG, "BluetoothAdapter not initialized");
                return;
            }
        }else{
            return;
        }
        if (aWriterUUIDString != null && aWriterUUIDString instanceof BluetoothGattCharacteristic) {
            ((BluetoothGattCharacteristic) aWriterUUIDString).setValue(out);
            mBluetoothGatt.writeCharacteristic(((BluetoothGattCharacteristic) aWriterUUIDString));
        } else if (aWriterUUIDString != null && aWriterUUIDString instanceof String) {
            BluetoothGattCharacteristic aCustomWriter = allCharacteristic.get(((String) aWriterUUIDString).toUpperCase());
            if (aCustomWriter != null) {
                aCustomWriter.setValue(out);
                mBluetoothGatt.writeCharacteristic(aCustomWriter);
            } else
                throw new NoWriterException(
                        "BluetoothGattCharacteristic not found according to UUID=" + aWriterUUIDString);
        } else {
            throw new NoWriterException("writer is null or unexpected error! UUID =" + aWriterUUIDString);
        }
    }

    private BluetoothGattCallback getGattCallback() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i(TAG, "gatt="+gatt+",status="+status+",newState="+newState);
                if (status != BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                    mState = STATE_DISCONNECTED;
                    setState(mState);
                    try {
                        LeChatService.this.stop();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Log.i(TAG, "Disconnected from GATT server.");
                } else if ( status == BluetoothGatt.GATT_SUCCESS  && newState == BluetoothProfile.STATE_CONNECTED && mState != STATE_CONNECTED) {
                    // Attempts to discover services after successful
                    // connection.
                    mBluetoothAdapter.cancelDiscovery();
                    mState = STATE_CONNECTED;
                    setState(STATE_CONNECTED);

                     Log.i(TAG, "Attempting to start service discovery and enable all notifications or indicators:" + gatt.discoverServices());
                } else if ( status == BluetoothGatt.GATT_SUCCESS  && newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mState = STATE_DISCONNECTED;
                    setState(mState);
                    try {
                        LeChatService.this.stop();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Log.i(TAG, "Disconnected from GATT server.");
                }else {
                    mState = STATE_DISCONNECTED;
                    setState(mState);
                    try {
                        LeChatService.this.stop();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Log.e(TAG, "GATT UNKNOWN ERROR , status = "+status );
                }
            }

            /**
             * get gatt object. use this method can get more information or
             * object , ex: get all service or characteristic object.
             *
             * @return
             */
            public BluetoothGatt getBluetoothGatt() {
                return mBluetoothGatt;
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                // InnerHandler innerHandler = new InnerHandler();
                if (allCharacteristic == null)
                    allCharacteristic = new HashMap<String, BluetoothGattCharacteristic>();
                else allCharacteristic.clear();
                //		Log.e(TAG, "onServicesDiscovered gatt=" + gatt + ",status=" + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (gatt == null) {
                        Log.e(TAG, "gatt is null");
                        Message msg1 = mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_FAIL);
                        mHandler.sendMessage(msg1);
                        return;
                    }

/*
                    for (BluetoothGattService service : mBluetoothGatt.getServices()) {
                        //	Log.d(TAG, "=>service uuid = " + service.getUuid());

                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            //			Log.d(TAG, "==>characteristic uuid under service= " + characteristic.getUuid());
                            allCharacteristic.put(characteristic.getUuid().toString().toUpperCase(), characteristic );
                            if (aSetNotifyOrIndicatorCharacteristicListenerUUID != null
                                    && aSetNotifyOrIndicatorCharacteristicListenerUUID.size() > 0) {
                                for (String sUUID : aSetNotifyOrIndicatorCharacteristicListenerUUID) {
                                    if (characteristic.getUuid().toString().equalsIgnoreCase(sUUID)) {

                                        Handler aHandler  = new Handler(Looper.getMainLooper());
                                        aHandler.postDelayed( new  NotifyOrIndicatorDelayRunnable(characteristic), iNotifyOrIndicatorDelayMilliseconds);
                                    }
                                }
                            }

                        }
                    }
*/
                    List<BluetoothGattService> aServices = mBluetoothGatt.getServices();
                    boolean bCheck = false;
                    for (String sUUID : aSetNotifyOrIndicatorCharacteristicListenerUUID) {
                        bCheck = false;
                    for (BluetoothGattService service : aServices) {
                        //	Log.d(TAG, "=>service uuid = " + service.getUuid());

                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            //			Log.d(TAG, "==>characteristic uuid under service= " + characteristic.getUuid());
                            allCharacteristic.put(characteristic.getUuid().toString().toUpperCase(), characteristic );
                            if (aSetNotifyOrIndicatorCharacteristicListenerUUID != null
                                    && aSetNotifyOrIndicatorCharacteristicListenerUUID.size() > 0) {

                                    if (characteristic.getUuid().toString().equalsIgnoreCase(sUUID)) {

                                        Handler aHandler  = new Handler(Looper.getMainLooper());
                                        aHandler.postDelayed( new  NotifyOrIndicatorDelayRunnable(characteristic), iNotifyOrIndicatorDelayMilliseconds);
                                        bCheck = true;
                                    }
                                }
                            }

                        }
                        if( !bCheck) {
                            Message aMessage = mHandler
                                    .obtainMessage(GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL, 1, -1);
                            Bundle aBundle = new Bundle();
                            aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING,sUUID);
                            aMessage.setData(aBundle);
                            mHandler.sendMessage(aMessage);
                        }
                    }

                } else {
                    Log.w(TAG, "onServicesDiscovered failed! status = " + status);
                    Message msg1 = mHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_FAIL);
                    mHandler.sendMessage(msg1);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                              int status) {
                bIsWrite = false;
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                             int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                // Log.i(TAG,"onCharacteristicChanged,uuid="+characteristic.getUuid()
                // );
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

                Log.i(TAG,
                        "onDescriptorWrite , descriptor's owner characteristic="
                                + descriptor.getCharacteristic().getUuid() + ", descriptor uuid = "
                                + descriptor.getUuid() + ",status=" + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Message aMessage = mHandler
                            .obtainMessage(GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS, 1, -1);
                    Bundle aBundle = new Bundle();
                    aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
                            descriptor.getCharacteristic().getUuid().toString());
                    aMessage.setData(aBundle);
                    mHandler.sendMessage(aMessage);
                } else {
                    Message aMessage = mHandler
                            .obtainMessage(GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL, 1, -1);
                    Bundle aBundle = new Bundle();
                    aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
                            descriptor.getCharacteristic().getUuid().toString());
                    aMessage.setData(aBundle);
                    mHandler.sendMessage(aMessage);
                }

                bIsWrite = false;
                nextWrite();
            }
        };
    }

    private synchronized void writeDescriptor(Object o) {
        if (sWriteQueue.isEmpty() && !bIsWrite) {
            doWriteDescibe(o);
        } else {
            sWriteQueue.add(o);
        }
    }

    private synchronized void nextWrite() {
        if (!sWriteQueue.isEmpty() && !bIsWrite) {
            doWriteDescibe(sWriteQueue.poll());
        }
    }

    private synchronized void doWriteDescibe(Object o) {
        if (o instanceof BluetoothGattCharacteristic) {
            bIsWrite = true;
            mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
        } else if (o instanceof BluetoothGattDescriptor) {
            bIsWrite = true;
            mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) o);
        } else {
            nextWrite();
        }
    }

    public static boolean isCharacteristicWriteable(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    /**
     * @return Returns <b>true</b> if property is Readable
     */
    public static boolean isCharacterisitcReadable(BluetoothGattCharacteristic pChar) {
        return ((pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    /**
     * @return Returns <b>true</b> if property is supports notification
     */
    public boolean isCharacterisiticNotifiable(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    public boolean isCharacterisiticIndicator(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;
    }

    public void setCharacteristicIndicator(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        try {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(UUID.fromString(GlobalSetting.Client_Characteristic_Configuration));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

            writeDescriptor(descriptor);
        } catch (Exception ee) {
            ee.printStackTrace();
        }

    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        try {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(UUID.fromString(GlobalSetting.Client_Characteristic_Configuration));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            writeDescriptor(descriptor);

        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    @Override
    public void setHandler(Handler aHandler) {
        mHandler = aHandler;
    }

    @Override
    public void setBlueToothAdapter(BluetoothAdapter aBlueToothAdapter) {
        mBluetoothAdapter = aBlueToothAdapter;
    }

    @Override
    public BluetoothAdapter getBlueToothAdapter() {
        return mBluetoothAdapter;
    }

    @Override
    public void read(Object objReaderChannel) throws NoBTReaderException {
        if( !GlobalSetting.getSimulation()) {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.e(TAG, "BluetoothAdapter not initialized");
                return;
            }
        }else{
            return;
        }
        if (objReaderChannel != null && objReaderChannel instanceof BluetoothGattCharacteristic) {
            mBluetoothGatt.readCharacteristic(((BluetoothGattCharacteristic) objReaderChannel));
        } else if (objReaderChannel != null && objReaderChannel instanceof String) {
            BluetoothGattCharacteristic aCustomReader = allCharacteristic.get(((String) objReaderChannel).toUpperCase());
            if (aCustomReader != null) {
                mBluetoothGatt.readCharacteristic(aCustomReader);
            }
        } else {
            throw new NoBTReaderException("writer is null or unexpected error! =" + objReaderChannel);
        }

    }

    /**
     * set nofity or indicator after  specify delay time when device connected , default is 200 milliseconds.
     * @param iMilliseconds
     */
    @Override
    public void setNotifyOrIndicatorDelayTime(int iMilliseconds) {
        iNotifyOrIndicatorDelayMilliseconds = iMilliseconds;
    }

    @Override
    public int getNotifyOrIndicatorDelayTime() {
        return iNotifyOrIndicatorDelayMilliseconds;
    }

}
