package inmethod.android.bt.command;

/**
 * bluetooth command string use for BLE Read characteristic.
 * @see BlueToothDeviceConnection
 * @author william chen 
 *
 */
public class ReadBTCommand extends BTCommand{
	
  private String sReaderCharacteristicUUID;
  
  /**
   * Classic BLUETOOTH is not required (always use 0x1101) or BLUETOOTH low energy can be customized (Characteristic UUID) .
   * @param sUUID
   */
  public void setReaderChannelUUID(String sUUID){
	  sReaderCharacteristicUUID = sUUID;  
  }
  
  
  /**
   * Classic BLUETOOTH is not required (always use 0x1101) or BLUETOOTH low energy can be customized (Characteristic UUID) .
   * @return Characteristic UUID
   */
  public String getReaderChannelUUID(){
	  return sReaderCharacteristicUUID;
  }
}
