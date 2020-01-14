# Android Bluetooth Framework v6.1.1b1 (20190823)

This Framework is designed for Handheld APP to communicate with Bluetooth device easily.

# Feature    
* [Simulation Mode](#markdown-header-how-to-start-in-simulation-mode)       

# Example    
* https://bitbucket.org/inmethod/simpleblehrbandmonitor    
  Very Simple example support standard BLE Heart Rate Band       

Note:
> This framework can not  send multiple BTCommands to remote bluetooth device  concurrently!    
> BTCommands will be queued and wait to be executed!


# Simple sequence diagram    
<<<<<<< HEAD
![SimpleFramework.png](https://github.com/WilliamFromTW/BlueToothFramework/blob/master/design/Overall.png?raw=true)
=======
![SimpleFramework.png](https://github.com/WilliamFromTW/BlueToothFramework/blob/NewUI/design/Overall.png?raw=true)
>>>>>>> f95d3063b28010da40409556ad4c666a7c7eab69

# Operation System Requirement    
* Android API 23 (Anroid 6.0)  or above

## Develop Environment    
* Android Studio 3.3 or above      

# Premission Requirement     

~~~~
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION"/>
~~~~

ACCESS_COARSE_LOCATION is run-time permission (android M or above)     
NOTE:    
> Android device should enable Location Service or it won't work even grant ACCESS_COARSE_LOCATION permission    

#### For example    

~~~~
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("This app needs location access");
        builder.setMessage("Please grant location access");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
          public void onDismiss(DialogInterface dialog) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
          };
        });
        builder.show();
      };
      LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
      if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        // show open gps message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setMessage("Please enable Location service(android 6)");
        builder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent enableGPSIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		    startActivity(enableGPSIntent);
          }
        });
        builder.show();
      }
    }
  }  
~~~~

## How To Implement This  Framework        
1.  [Discovery BlueTooth Device](#markdown-header-discovery-blueTooth-device)            
2.  [Connect device](#markdown-header-connect-device)        
3.  [Send your costomized BTCommands](#markdown-header-send-your-costomized-btcommands)      


#### Discovery BlueTooth Device        

~~~~
   // Get discovery service instance
   IDiscoveryService aIBlueToothDiscoveryService = LeDiscoveryService.getInstance();

   // Set discovery filter
   Vector<String> aBlueToothDeviceNameFilter = new Vector<String>();
   aBlueToothDeviceNameFilter.add(< DEVICE NAME>);
   aIBlueToothDiscoveryService.setBlueToothDeviceNameFilter(aBlueToothDeviceNameFilter);

   // Set CallBack handler
   aIBlueToothDiscoveryService.setCallBackHandler(new MyBlueToothDiscoveryServiceCallbackHandler());

   // set scan time out , default is 12000 milliseconds
   aIBlueToothDiscoveryService.setScanTimeout(12000);

   try {
     // start service will trigger method "StartDiscoveryServiceSuccess()" in BlueToothDiscoveryServiceCallbackHandler
     aIBlueToothDiscoveryService.startService();
   } catch (Exception e) {
     e.printStackTrace();
   }

   public static class MyBlueToothDiscoveryServiceCallbackHandler extends BlueToothDiscoveryServiceCallbackHandler {

        @Override
        public void StartServiceStatus(boolean bStatus , int iCode) {
          if(bStatus && iCode==DiscoveryServiceCallbackHandler.START_SERVICE_SUCCESS){
            Log.d(TAG, "StartDiscoveryServiceSuccess!");
            if (aIBlueToothDiscoveryService.isRunning())  aIBlueToothDiscoveryService.doDiscovery();
          }else if(!bStatus && iCode==DiscoveryServiceCallbackHandler.START_SERVICE_BLUETOOTH_NOT_ENABLE){
            Toast.makeText(activity, "SERVICE_BLUETOOTH_NOT_ENABLE ", Toast.LENGTH_SHORT).show();
          }
        }

        @Override
        public void DeviceDiscoveryStatus(boolean bStatus, BTInfo aBTInfo) {
          if(bStatus){
            // cancel discovery
            aIBlueToothDiscoveryService.cancelDiscovery();
            // enable notification
            ArrayList<String> aNotificationUUIDArray = new ArrayList<String>();
            aNotificationUUIDArray.add("your notification uuid");
            aIBlueToothChatService = new LeChatService(BluetoothAdapter.getDefaultAdapter(),    activity , aNotificationUUIDArray);
            // create new connection object and set up call back handler
            aBlueToothDeviceConnection = new DeviceConnection(aBTInfo, activity, aIBlueToothChatService, new MyBlueToothConnectionCallbackHandler());
            // Connect to device
            aBlueToothDeviceConnection.connect();
            Toast.makeText(activity, "device=" + aBTInfo.getDeviceName() + ",address=" + aBTInfo.getDeviceAddress(), Toast.LENGTH_SHORT).show();

          }else if(!bStatus){
            Log.d(TAG, "device not found!");
            Toast.makeText(activity, "device not found!", Toast.LENGTH_SHORT).show();
          }
        }

    }   

~~~~

#### Connect device        
     
  When bluetooth device found in Step 1 , app can get device information (BTInfo) and try to connect device.

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
   public void DeviceConnectionStatus(boolean bStatus, BTInfo aBTInfo) {

     if(bStatus){
       Toast.makeText(activity, "Device Connected", Toast.LENGTH_SHORT).show();
     }else{
       Toast.makeText(activity, "Connection lost!", Toast.LENGTH_SHORT).show();
       if (aBlueToothDeviceConnection != null && aBlueToothDeviceConnection.isConnected()) {
         Log.d(TAG, "stop connection!");
         aBlueToothDeviceConnection.stop();
       }
     }
  }

    @Override
    public void NotificationStatus(boolean bStatus,BTInfo aBTInfo, String sNotificationUUID) {
      if(bStatus){
        Toast.makeText(activity, "NotificationEnabled", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "NotificationEnabled sNotificationUUID=" + sNotificationUUID);
        if (sNotificationUUID != null && sNotificationUUID.equalsIgnoreCase("your notification uuid)) {
          BTCommands aBTCommands = <your customized BTCommands>
          aBTCommands.setCallBackHandler(<your Command Handler>);
          if (aBlueToothDeviceConnection.isConnected()) {
            Toast.makeText(activity, "send BT commands to device", Toast.LENGTH_SHORT).show();
            aBlueToothDeviceConnection.sendBTCommands(aBTCommands);
          }
        }
      }else if(!bStatus){
        Log.d(TAG, "NotificationEnableFail");
      }
   }
 }  
~~~~

#### Send your costomized BTCommands    
  Send Your costomized BTCommands to Device and receive responsed data
  
  In Step 2, Device connected will trigger callback method "DeviceNotificationOrIndicatorEnableSuccess" , we can send our costomized "BTCommands" to device.    
  If device responsed data , callback method "handleCommandResponsedMessage()" will be triggered.    

~~~~
  BTCommands aBTCommands = new MyBTCommands();
  aBTCommands.setCallBackHandler(new MyBlueToothCommandCallbackHandler());
  Toast.makeText(activity, "send BT commands to device", Toast.LENGTH_SHORT).show();
  aBlueToothDeviceConnection.sendBTCommands(aBTCommands);
  
  public class MyBlueToothCommandCallbackHandler extends inmethod.android.bt.handler.CommandCallbackHandler {   
  
      @Override
      public void handleCommandResponsedMessage(Message msg) {
        switch(msg.what){
          <MyBTCommands's msg.what>:  // see BTCommands example below 
          break;
        }
     }
	
  }
~~~~

#### How to create your customized BTCommands    

~~~~

public class MyBTCommands extends BTCommands {

  private final static String MY_WRITER_UUID = "0000xxxx-0000-1000-8000-00805f9b34fb";
  private final static int MY_CALLBACK_SUCCESS = "10000";
  private final static int MY_CALLBACK_TIMEOUT = "10001";

  public MyBTCommands() {
       
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

    
    
## How To Start In Simulation Mode         
*  To enable simulation   
inmethod.android.bt.GlobalSetting.setSimulation(true);    
or     
inmethod.android.bt.GlobalSetting.setSimulation(true,"bluetooth advertisement data");        

* Setup BTCommands responsed data & notification uuid from remote device     
aBTCommands.setSimulationNotificationUUID("0000xxxx-0000-1000-8000-00805f9b34fb");    
aBTCommands.setSimulationResponsedData(new byte[]{'a','b'});    

![SimulationMode.png](https://bitbucket.org/repo/jagqny/images/1273069560-SimulationMode.png)
