# Android Bluetooth Framework v5.0 Released (20160521) #

This Framework is designed for Handheld APP to communicate with slave Bluetooth device easily.

# Example
* https://bitbucket.org/inmethod/simpleblehrbandmonitor    
  Very Simple example support standard BLE Heart Rate Band    
* https://bitbucket.org/inmethod/blehrbandmonitor    
  Support MinBand1s , standard BLE heart Rate Band    

Note:
> This framework can not  send multiple BTCommands to slave bluetooth device  concurrently!    
> It send one BTCommands at one time , other BTCommands will be queued and wait to execute!    


# Simple sequence diagram
![SimpleFramework.png](https://bitbucket.org/repo/jagqny/images/3752253336-SimpleFramework.png)

# Operation System Requirement #
* Android 4.3 or above
* Android Wear 5.0 or above

# Feature
* Support Classic Bluetooth(SPP) and Bluetooth Low Energy

## Develop Environment
* Android Studio 2.0 or above    

# Premission Requirement #

~~~~
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION"/>
~~~~

ACCESS_COARSE_LOCATION is run-time permission (android M or above)     
NOTE:
> Android device should enable Location Service or it won't work even grant ACCESS_COARSE_LOCATION permission    

#### For example  ####

~~~~

 private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
 protected void onCreate(Bundle savedInstanceState) {
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
     requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
   }
 }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }   
~~~~

## How To Use This  Framework ##

There are 3 Sections

* Section 1
 Discovery Slave BlueTooth Device    

~~~~
   // Get discovery service instance
   IDiscoveryService aIBlueToothDiscoveryService = LeDiscoveryService.getInstance();

   // Set discovery filter
   Vector<String> aBlueToothDeviceNameFilter = new Vector<String>();
   aBlueToothDeviceNameFilter.add(<SLAVE DEVICE NAME>);
   aIBlueToothDiscoveryService.setBlueToothDeviceNameFilter(aBlueToothDeviceNameFilter);

   // Set CallBack handler
   aIBlueToothDiscoveryService.setCallBackHandler(new MyBlueToothDiscoveryServiceCallbackHandler());

   try {
     // start service will trigger method "StartDiscoveryServiceSuccess()" in BlueToothDiscoveryServiceCallbackHandler
     aIBlueToothDiscoveryService.startService();
   } catch (Exception e) {
     e.printStackTrace();
   }

   public static class MyBlueToothDiscoveryServiceCallbackHandler extends BlueToothDiscoveryServiceCallbackHandler {

        /*
		 * Online Device Not Found ! After IBlueToothDiscoveryService.doDiscovery()
		 */
        @Override
        public void OnlineDeviceNotFound() {
            Log.d(TAG, "device not found!");
            Toast.makeText(activity, "device not found!", Toast.LENGTH_SHORT).show();
        }

        /*
		 * Online Device Found ! after IBlueToothDiscoveryService.doDiscovery()
         * @see IBlueToothDiscoveryService.doDiscovery()
         */
        @Override
        public void getOnlineDevice(BTInfo aBTInfo) {
            // cancel discovery
            aIBlueToothDiscoveryService.cancelDiscovery();
            Toast.makeText(activity, "device found ! Name = " + aBTInfo.getDeviceName() + ", Address=" + aBTInfo.getDeviceAddress(), Toast.LENGTH_SHORT).show();

        }

        /*
         *
         * @see
         * inmethod.android.bt.handler.BlueToothDiscoveryServiceCallbackHandler#StartServiceSuccess()
         */
        @Override
        public void StartServiceSuccess() {
            Log.d(TAG, "StartDiscoveryServiceSuccess!");
            // doDiscovery will discovery slave device and trigger OnlineDeviceNotFound() or getOnlineDevice(BTInfo)
            aIBlueToothDiscoveryService.doDiscovery();
        }

    }   

~~~~

* Section 2   
  Connect to slave device.    
     
  When slave bluetooth device found in Section 1 , app can get device information (BTInfo) and try to connect device.

~~~~
 // Set Device Notification or Indicator UUID ()
 ArrayList<String> aReaderUUIDArray = new ArrayList<String>();
 // 0000xxxx-0000-1000-8000-00805f9b34fb is notification uuid
 aReaderUUIDArray.add("0000xxxx-0000-1000-8000-00805f9b34fb"); 

 // Create ble chat service 
 IChatService  aIBlueToothChatService = new LeChatService(BluetoothAdapter.getDefaultAdapter(), activity , aReaderUUIDArray);

 // Create connection object and setup connection call back handler
 DeviceConnection aBlueToothDeviceConnection = new DeviceConnection(aBTInfo, activity, aIBlueToothChatService, new MyBlueToothConnectionCallbackHandler());

 // connect to device
 aBlueToothDeviceConnection.connect();

 public static class MyBlueToothConnectionCallbackHandler extends ConnectionCallbackHandler {

 @Override
 public void DeviceConnected(BTInfo aBTInfo) {
    Toast.makeText(activity, "Device Connected", Toast.LENGTH_SHORT).show();
 }

 @Override
 public void DeviceConnectionLost(BTInfo aBTInfo) {
     Toast.makeText(activity, "Connection lost!", Toast.LENGTH_SHORT).show();
  }

 @Override
 public void NotificationEnableFail(BTInfo arg0, String sErrorMessage) {
     Log.d(TAG, "NotificationEnableFail");
 }

 @Override
 public void NotificationEnabled(BTInfo aBTInfo, String sReaderUUID) {
    Log.d(TAG, "NotificationEnabled sReaderUUID=" + sReaderUUID);
 }
~~~~

* Section 3
  Send "BTCommands" to Device and receive response data
  
  In Section 2, Device connected will trigger callback method "DeviceNotificationOrIndicatorEnableSuccess" , we can send "BTCommands" to device.    
  If device response data , callback method "handleCommandResponseMessage()" will be triggered.    

~~~~
  BTCommands aBTCommands = new MyBTCommands(aBTInfo);
  aBTCommands.setCallBackHandler(new MyBlueToothCommandCallbackHandler());
  Toast.makeText(activity, "send BT commands to device", Toast.LENGTH_SHORT).show();
  aBlueToothDeviceConnection.sendBTCommands(aBTCommands);
  
  public class MyBlueToothCommandCallbackHandler extends inmethod.android.bt.handler.CommandCallbackHandler {   
  
    @Override
    public void handleCommandResponseMessage(Message msg) {
      switch(msg.what){
         <MyBTCommands's msg.what>:  // see BTCommands example below 
         break;
      }
	}
	
  }
~~~~

* BTCommands example

~~~~

public class MyBTCommands extends BTCommands {

  private final static String MY_WRITER_UUID = "0000xxxx-0000-1000-8000-00805f9b34fb";
  private final static int MY_CALLBACK_SUCCESS = "10000";
  private final static int MY_CALLBACK_TIMEOUT = "10001";

  public MyBTCommands(BTInfo aBTInfo) {
        super(aBTInfo);
        byte[] byteCmd = new byte[4];
        byteCmd[0] = (byte) 0x6F;
        byteCmd[1] = (byte) 0x53;
        byteCmd[2] = (byte) 0x01;
        byteCmd[3] = (byte) 0x35;

        BTCommand aCmd = new BTCommand(MY_WRITER_UUID,byteCmd);
        addCommand(aCmd);
        setTimeout(800);
  }
  
  /**
    *
    * @param byteData reponse data
    * @param aChannel can be Characteristic UUID object or UUID String
    * @throws Exception
    */
  @Override
  public void getData(byte byteData, Object aNotificationUUID) throws Exception{
    
    if (!isFinished()) {
      Message aMessage = getCallBackHandler().obtainMessage( MY_CALLBACK_SUCCESS, 1, -1);
      Bundle aBundle = new Bundle();
      aMessage.setData(aBundle);
      // callback method will be triggered 
      getCallBackHandler().sendMessage(aMessage);
      setFinished(true);
    }
  }
  
  @Override
  public void handleTimeout() throws Exception{
    if (!isFinished()) {
	  Message aMessage = getCallBackHandler().obtainMessage( MY_CALLBACK_TIMEOUT, 1, -1);
	  Bundle aBundle = new Bundle();
	  aMessage.setData(aBundle);
	  // callback method will be triggered 
	  getCallBackHandler().sendMessage(aMessage);
	  setFinished(true);
	}
  }
	
}
~~~~