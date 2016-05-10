package inmethod.android.bt.command;

import inmethod.android.bt.DeviceConnection;

/**
 * bluetooth command string use for BLE Read characteristic.
 * @see DeviceConnection
 * @author william chen 
 *
 */
public class BTReadCommand extends BTCommand{
	
  private String sReaderCharacteristicUUID;

    public BTReadCommand(String sReaderCharacteristicUUID){
        super(null,null); // no writer UUID
        this.sReaderCharacteristicUUID = sReaderCharacteristicUUID;
    }
  /**
   * Classic BLUETOOTH is not required (always use 0x1101) or BLUETOOTH low energy can be customized (Characteristic UUID) .
   * @param sReaderCharacteristicUUID
   */
  public void setReaderChannelUUID(String sReaderCharacteristicUUID){
	  this.sReaderCharacteristicUUID = sReaderCharacteristicUUID;
  }
  
  
  /**
   * Classic BLUETOOTH is not required (always use 0x1101) or BLUETOOTH low energy can be customized (Characteristic UUID) .
   * @return Characteristic UUID
   */
  public String getReaderChannelUUID(){
	  return sReaderCharacteristicUUID;
  }
}
