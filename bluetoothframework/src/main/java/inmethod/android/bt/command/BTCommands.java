package inmethod.android.bt.command;

import java.math.BigInteger;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.BlueToothDeviceConnection;
import inmethod.android.bt.BlueToothGlobalSetting;
import inmethod.android.bt.exception.NoCallBackHandlerException;
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
	protected BTInfo aBTInfo = null;
	protected boolean bFinished = false;
	protected boolean bIsTransferData = false;

	private int iTimeout = 0;
	protected IBlueToothChatService aBTChat = null;
	protected BlueToothCommandCallbackHandler mCallBackHandler = null;
	protected BlueToothDeviceConnection aBlueToothDeviceConnection = null;

	public void setBTChat(IBlueToothChatService mBTChat) {
		aBTChat = mBTChat;
	}

	public IBlueToothChatService getBTChat() {
		return aBTChat;
	}

	public BlueToothCommandCallbackHandler getCallBackHandler() throws NoCallBackHandlerException {
		if (this.mCallBackHandler == null)
			throw new NoCallBackHandlerException("handler is null!");
		else
			return this.mCallBackHandler;
	}

	public abstract void setCallBackHandler(BlueToothCommandCallbackHandler aCallBack);

	public abstract void getData(int iData, Object aChannel) throws Exception;

	public abstract boolean isDataTransferingCompleted();

	public abstract void setBTInfo(BTInfo aInfo);

	public abstract BTInfo getBTInfo();

	public abstract void handleTimeout() throws Exception;

	/**
	 * send extra message to remote device.
	 * 
	 * @param aExceptionObject
	 */
	public abstract void sendExtraCommands(Object aExceptionObject);

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
	public boolean getFinished() {
		return bFinished;
	}

	/**
	 * setting timeout for bluetooth commands , if timeout is zero , connection
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
	
	

}
