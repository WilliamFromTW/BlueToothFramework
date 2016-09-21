package inmethod.android.bt.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.GlobalSetting;

public class CommandCallbackHandler extends Handler {
	public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();

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
	public  void handleCommandResponsedMessage(Message msg){}

}
