# README #

This project "Android Bluetooth Framework" is designed for helping APP to communicate with slave Bluetooth device easily.

Note:
>  This framework can not  send multiple BTCommands to slave bluetooth device  concurrently!    
> It send one BTCommands at one time , other BTCommands will be queued and wait to execute!

# Simple sequence diagram
![Simple Framework.png](https://bitbucket.org/repo/jagqny/images/364558861-Simple%20Framework.png)

# System Requirement #
* Android 4.3 or above
* Support Classic Bluetooth and Bluetooth Low Energy
* Android Studio 2.0 or above

## How To Use This  Framework ##

There are 3 Sections

* Section 1
 Discovery Slave BlueTooth Device    

~~~~
   // Get discovery service instance
   IBlueToothDiscoveryService aIBlueToothDiscoveryService = BlueToothLeDiscoveryService.getInstance();

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
         * inmethod.android.bt.handler.BlueToothDiscoveryServiceCallbackHandler#StartDiscoveryServiceSuccess()
         */
        @Override
        public void StartDiscoveryServiceSuccess() {
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
 IBlueToothChatService  aIBlueToothChatService = new BlueToothLeChatService(BluetoothAdapter.getDefaultAdapter(), activity , aReaderUUIDArray);

 // Create connection object and setup connection call back handler
 BlueToothDeviceConnection aBlueToothDeviceConnection = new BlueToothDeviceConnection(aBTInfo, activity, aIBlueToothChatService, new MyBlueToothConnectionCallbackHandler());

 // connect to device
 aBlueToothDeviceConnection.connect();

 public static class MyBlueToothConnectionCallbackHandler extends BlueToothConnectionCallbackHandler {

 @Override
 public void DeviceConnected(BTInfo aBTInfo) {
    Toast.makeText(activity, "Device Connected", Toast.LENGTH_SHORT).show();
 }

 @Override
 public void DeviceConnectionLost(BTInfo aBTInfo) {
     Toast.makeText(activity, "Connection lost!", Toast.LENGTH_SHORT).show();
  }

 @Override
 public void DeviceNotificationOrIndicatorEnableFail(BTInfo arg0, String sErrorMessage) {
     Log.d(TAG, "DeviceNotificationOrIndicatorEnableFail");
 }

 @Override
 public void DeviceNotificationOrIndicatorEnableSuccess(BTInfo aBTInfo, String sReaderUUID) {
    Log.d(TAG, "DeviceNotificationEnableSuccess sReaderUUID=" + sReaderUUID);
 }
~~~~

* Section 3
  Send "BTCommands" to Device and receive responsed data
  
  In Section 2, Device connected will trigger callback method "DeviceNotificationOrIndicatorEnableSuccess" , we can send "BTCommands" to device.    
  If device response data , callback method "handleCommandResponsedMessage()" will be triggered.    

~~~~
  BTCommands aBTCommands = new MyBTCommands(aBTInfo);
  aBTCommands.setCallBackHandler(new MyBlueToothCommandCallbackHandler());
  Toast.makeText(activity, "send BT commands to device", Toast.LENGTH_SHORT).show();
  aBlueToothDeviceConnection.sendBTCommand(aBTCommands);
  
  public class MyBlueToothCommandCallbackHandler extends inmethod.android.bt.handler.BlueToothCommandCallbackHandler {   
  
    @Override
    public void handleCommandResponsedMessage(Message msg) {
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
        BTCommand aCmd = new BTCommand();
        aCmd.setWriterChannelUUID(MY_WRITER_UUID);  
        byte[] byteCmd = new byte[4];
        byteCmd[0] = (byte) 0x6F;
        byteCmd[1] = (byte) 0x53;
        byteCmd[2] = (byte) 0x01;
        byteCmd[3] = (byte) 0x35;
        aCmd.setCommandString(byteCmd);
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