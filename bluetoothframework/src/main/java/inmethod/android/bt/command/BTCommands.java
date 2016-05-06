package inmethod.android.bt.command;

import java.util.ArrayList;

import inmethod.android.bt.BTInfo;
import inmethod.android.bt.DeviceConnection;
import inmethod.android.bt.handler.CommandCallbackHandler;
import inmethod.android.bt.interfaces.IChatService;
import inmethod.commons.util.HexAndStringConverter;

/**
 * abstract class,Used to extend and create new bluetooth command.
 * 
 * @author william chen
 *
 */
public abstract class BTCommands {

	protected ArrayList<BTCommand> aCommandList = new ArrayList<BTCommand>();
	protected ArrayList<BTCommand> aOriginalCommandList = new ArrayList<BTCommand>();
	private BTInfo aBTInfo = null;
	private boolean bFinished = false;

	private int iTimeout = 8;
	private IChatService aBTChat = null;
	private CommandCallbackHandler mCallBackHandler = null;
	private DeviceConnection aDeviceConnection = null;

	public void setBTChat(IChatService mBTChat) {
		aBTChat = mBTChat;
	}

	public IChatService getBTChat() {
		return aBTChat;
	}

	public CommandCallbackHandler getCallBackHandler()  {
			return this.mCallBackHandler;
	}

	public BTCommands(BTInfo aBTInfo){
	   setBTInfo(aBTInfo);
	}
	public void setBTInfo(BTInfo aInfo){ this.aBTInfo = aInfo; }

	public BTInfo getBTInfo(){return aBTInfo;}

	/**
	 * send extra message to remote device.
	 * 
	 * @param aExceptionObject
	 */
	public void sendExtraCommands(Object aExceptionObject){}



	/**
	 * set Current bt connection.
	 * @param aDeviceConnection
     */
	public void setCurrentConnection(DeviceConnection aDeviceConnection) {
		this.aDeviceConnection = aDeviceConnection;
	}

	/**
	 * return current bt connection.
	 * @return
     */
	public DeviceConnection getCurrentConnection() {
		return this.aDeviceConnection;
	}

	/**
	 * If commands is not finished , this method can cancel command.
	 */
	public void cancelCommand() {
		if (aCommandList != null)
			aCommandList.clear();
	}

	/**
	 * if commands all execute , set true;
	 * 
	 * @param bFinished
	 */
	public void setFinished(boolean bFinished) {
		this.bFinished = bFinished;
	}

	/**
	 * get commands status
	 * 
	 * @return true: finished , false: not finished
	 */
	public boolean isFinished() {
		return bFinished;
	}

	/**
	 * Setting timeout for bluetooth commands , if timeout is zero , connection
	 * will not close connection when time out. Default timeout is 8 seconds.
	 * 
	 * @param iT
	 */
	public void setTimeout(int iT) {
		iTimeout = iT;
	}

	/**
	 * get command timeout information.
	 * 
	 * @return
	 */
	public int getTimeout() {
		return iTimeout;
	}

	/**
	 * add command
	 * 
	 * @param aCmd
	 *            single bluetooth cmd
	 */
	public void addCommand(BTCommand aCmd) {
		aCommandList.add(aCmd);
		aOriginalCommandList = null;
		aOriginalCommandList = (ArrayList<BTCommand>) aCommandList.clone();
	}

	public ArrayList<BTCommand> getOriginallCommandList() {
		return aOriginalCommandList;
	}

	public String toString() {
		String sReturn = "";
		for (BTCommand aBTCmd : aOriginalCommandList) {
			if (aBTCmd.getCommandString() != null)
				sReturn = sReturn + "\nasc(" + new String(aBTCmd.getCommandString()) + "),hex("
						+ HexAndStringConverter.convertHexByteToHexString(aBTCmd.getCommandString()) + ")";
		}
		return sReturn;
	}

	/**
	 * get BTCommand list
	 * 
	 * @return a list of BTCommand
	 */
	public ArrayList<BTCommand> getCommandList() {
		return aCommandList;
	}

	public void setCallBackHandler(CommandCallbackHandler aCallBack){mCallBackHandler = aCallBack;}

	/**
	 *
	 * @param byteData reponsed data
	 * @param aNotificationChannelUUID can be Characteristic UUID object or UUID String
	 * @throws Exception
     */
	public abstract void getData(byte byteData, Object aNotificationChannelUUID) throws Exception;

	public abstract void handleTimeout() throws Exception;

}
