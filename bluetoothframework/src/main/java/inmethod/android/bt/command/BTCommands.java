package inmethod.android.bt.command;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.BlueToothDeviceConnection;
import inmethod.android.bt.BlueToothGlobalSetting;
import inmethod.android.bt.handler.BlueToothCommandCallbackHandler;
import inmethod.android.bt.interfaces.IBlueToothChatService;
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
	private boolean bIsTransferData = false;

	private int iTimeout = 8;
	private IBlueToothChatService aBTChat = null;
	private BlueToothCommandCallbackHandler mCallBackHandler = null;
	private BlueToothDeviceConnection aBlueToothDeviceConnection = null;

	public void setBTChat(IBlueToothChatService mBTChat) {
		aBTChat = mBTChat;
	}

	public IBlueToothChatService getBTChat() {
		return aBTChat;
	}

	public BlueToothCommandCallbackHandler getCallBackHandler()  {
			return this.mCallBackHandler;
	}


	public boolean isDataTransferingCompleted(){return bFinished;};

	public void setBTInfo(BTInfo aInfo){ this.aBTInfo = aInfo; }

	public BTInfo getBTInfo(){return aBTInfo;}

	/**
	 * send extra message to remote device.
	 * 
	 * @param aExceptionObject
	 */
	public void sendExtraCommands(Object aExceptionObject){}



	public void setTransferDataStatus(boolean bTrueFalse) {
		bIsTransferData = bTrueFalse;
	}

	public boolean getTransferDataStatus() {
		return bIsTransferData;
	}

	public void setCurrentConnection(BlueToothDeviceConnection aBlueToothDeviceConnection) {
		this.aBlueToothDeviceConnection = aBlueToothDeviceConnection;
	}

	public void beginTransferData() {
		if (mCallBackHandler != null) {
			Message aMessage = mCallBackHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_STATUS_TRANSFER_DATA, 1, -1);
			Bundle aBundle = new Bundle();
			aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
			aMessage.setData(aBundle);
			mCallBackHandler.sendMessage(aMessage);
		}
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

	public void setCallBackHandler(BlueToothCommandCallbackHandler aCallBack){mCallBackHandler = aCallBack;}

	/**
	 *
	 * @param byteData reponsed data
	 * @param aChannel can be Characteristic UUID object or UUID String
	 * @throws Exception
     */
	public abstract void getData(byte byteData, Object aChannel) throws Exception;

	public abstract void handleTimeout() throws Exception;

}
