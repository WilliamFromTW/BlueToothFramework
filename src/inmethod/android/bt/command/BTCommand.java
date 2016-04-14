package inmethod.android.bt.command;

/**
 * bluetooth command string.
 * 
 * @author william chen
 *
 */
public class BTCommand {

	private byte[] byteCmd;
	private int iDelayTime;
	private String sWriterCharacteristicUUID;
	// channel id

	public BTCommand() {
		iDelayTime = 0;
	}

	/**
	 * Classic BLUETOOTH is not required (always use 0x1101) or BLUETOOTH low
	 * energy can be customized (Characteristic UUID) .
	 * 
	 * @param sUUID
	 */
	public void setWriterChannelUUID(String sUUID) {
		sWriterCharacteristicUUID = sUUID;
	}

	/**
	 * Classic BLUETOOTH is not required (always use 0x1101) or BLUETOOTH low
	 * energy can be customized (Characteristic UUID) .
	 * 
	 * @return Characteristic UUID
	 */
	public String getWriterChannelUUID() {
		return sWriterCharacteristicUUID;
	}

	/**
	 * get command string.
	 * 
	 * @return
	 */
	public byte[] getCommandString() {
		if (byteCmd != null)
			return byteCmd;
		else
			return (new String("No Write Command")).getBytes();
	}

	/**
	 * set command string
	 * 
	 * @param byteCmd
	 */
	public void setCommandString(byte[] byteCmd) {
		this.byteCmd = byteCmd;
	}

	/**
	 * get delay time when this command send to bluetooth device
	 * 
	 * @return
	 */
	public int getDelayTime() {
		return iDelayTime;
	}

	/**
	 * set delay time when this command send to bluetooth device
	 * 
	 * @param iDelayTime
	 */
	public void setDelayTime(int iDelayTime) {
		this.iDelayTime = iDelayTime;
	}

}
