package inmethod.android.bt.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.BlueToothGlobalSetting;

public abstract class BlueToothCommandCallbackHandler extends Handler {
	public final String TAG = BlueToothGlobalSetting.TAG + "/" + getClass().getSimpleName();

	Bundle aBundle = null;
	BTInfo aInfo = null;
	byte[] byteData = null;
	String sErrorMessage = "";

	public void handleMessage(Message msg) {
		switch (msg.what) {
		default:
			handleCommandResponsedMessage(msg);
			break;
		}
	}

	/**
	 * handler extra message
	 * 
	 * @param msg
	 */
	public abstract void handleCommandResponsedMessage(Message msg);

}
