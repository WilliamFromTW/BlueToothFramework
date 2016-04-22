package inmethod.android.bt;

import java.util.Arrays;
import java.util.LinkedList;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import inmethod.android.bt.classic.BlueToothChatService;
import inmethod.android.bt.command.BTCommand;
import inmethod.android.bt.command.BTCommands;
import inmethod.android.bt.command.ReadBTCommand;
import inmethod.android.bt.exception.NoBTReaderException;
import inmethod.android.bt.exception.NoWriterException;
import inmethod.android.bt.exception.NoUuidException;
import inmethod.android.bt.handler.BlueToothConnectionCallbackHandler;
import inmethod.android.bt.interfaces.IBlueToothChatService;
import inmethod.android.bt.le.BlueToothLeChatService;
import inmethod.commons.util.HexAndStringConverter;

public class BlueToothDeviceConnection {

	public final String TAG = BlueToothGlobalSetting.TAG + "/" + getClass().getSimpleName();

	protected static final boolean D = true;

	private boolean bIsConnected = false;
	private BTCommands aCommands = null;
	private int iCommandSize = 0;
	private boolean bTriggerConnectedAndThenSendCommand = false;
	private boolean bStopWatchDog = false;
	private boolean bFirstBTCommands = true;
	private BTInfo aBTInfo = null;
	private BluetoothAdapter aBluetoothAdapter = null;
	private IBlueToothChatService mBTChat = null;
	private LinkedList<BTCommands> aBTCommandsList = null;
	private BlueToothConnectionCallbackHandler aConnectionHandler = null;
	private Thread aWatchDogThread = null;
	private Context aContext = null;

	private BlueToothDeviceConnection() {
	}

	/**
	 * 
	 * @param aBtInfo
	 * @param context
	 * @param aBTChat
	 * @param aCallBackHandler
	 */
	public BlueToothDeviceConnection(BTInfo aBtInfo, Context context, IBlueToothChatService aBTChat,
			BlueToothConnectionCallbackHandler aCallBackHandler) {
		aBTCommandsList = new LinkedList<BTCommands>();
		aBTInfo = aBtInfo;
		aBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		aConnectionHandler = aCallBackHandler;
		aContext = context;
		mBTChat = aBTChat;
		mBTChat.setHandler(mHandler);
		mBTChat.start();
	}

	/**
	 * set Connection call back handler.
	 */
	public void setCallBackHandler(BlueToothConnectionCallbackHandler aHandler) {
		aConnectionHandler = aHandler;
	}

	/**
	 * get blue tooth info of this connection
	 * 
	 * @return bluetooth info
	 */
	public BTInfo getBTInfo() {
		return aBTInfo;
	}

	/**
	 * connect to bluetooth device.
	 */
	public void connect() {

		// Attempt to connect to the device
		mBTChat.connect(aBTInfo.getDeviceAddress());

		// watch dog to check new command and execute command.
		aWatchDogThread = new Thread() {
			Runnable r = null;

			public void run() {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (!bStopWatchDog) {

					if (!bFirstBTCommands && !aCommands.getFinished()) {
						Log.d(TAG, "current running cmds=" + aCommands.toString());
						try {
							sleep(800);
						} catch (InterruptedException e) {
						}
					}

					if (mBTChat != null && mBTChat.getState() != BlueToothChatService.STATE_CONNECTED) {
						aBTCommandsList.clear();
					}

					if (aCommands == null || aBTCommandsList == null || aBTCommandsList.size() == 0) {
						// continue;
						/*
						 * if( aCommands.getFinished()){ bStopWatchDog = true;
						 * return; } else continue;
						 */
						try {
							// Log.d(TAG, "no bt command , so let me sleep about
							// 60s , thanks");
							sleep(60000);
						} catch (InterruptedException e) {
							Log.d(TAG, "got bt command , stop sleep");
							;// Thread.currentThread ().interrupt ();
							;// break;
						}
					}

					// Log.i(TAG, "connect status =
					// "+isConnected()+"!,aBTCommandsList.size()="+aBTCommandsList.size()+",aCommands.getFinished()="+aCommands.getFinished());
					if (!isConnected() && aBTCommandsList != null && aBTCommandsList.size() > 0) {
						aBTCommandsList.clear();
					}

					if (isConnected() && aBTCommandsList.size() > 0 && (aCommands.getFinished() || bFirstBTCommands)) {
						if (aCommands.getFinished()) {
							Log.i(TAG, "remove timeout thread because command is finished!");
							mHandler.removeCallbacksAndMessages(null);
						}
						bFirstBTCommands = false;
						aCommands = aBTCommandsList.poll();
						aCommands.setBTInfo(aBTInfo);
						iCommandSize = aCommands.getCommandList().size();
						if (aCommands.getCommandList().size() >= 1) {
							if (!aCommands.getTransferDataStatus()) {
								aCommands.setTransferDataStatus(true);
								aCommands.beginTransferData();
							}
							try {
								mHandler.sendMessage(mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_SEND_DATA, 1, -1));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						if (aCommands.getCommandList().size() >= 2) {
							try {
								mHandler.sendMessageDelayed(
										mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_SEND_DATA, 2, -1),
										((BTCommand) aCommands.getCommandList().get(0)).getDelayTime());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						if (aCommands.getCommandList().size() >= 3) {
							try {
								mHandler.sendMessageDelayed(
										mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_SEND_DATA, 3, -1),
										((BTCommand) aCommands.getCommandList().get(0)).getDelayTime()
												+ ((BTCommand) aCommands.getCommandList().get(1)).getDelayTime());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (aCommands.getCommandList().size() >= 4) {
							try {
								mHandler.sendMessageDelayed(
										mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_SEND_DATA, 4, -1),
										((BTCommand) aCommands.getCommandList().get(0)).getDelayTime()
												+ ((BTCommand) aCommands.getCommandList().get(1)).getDelayTime()
												+ ((BTCommand) aCommands.getCommandList().get(2)).getDelayTime());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						if (aCommands.getTimeout() > 0) {
							if (D)
								Log.i(TAG, "set command timeout = " + aCommands.getTimeout());
							r = new Runnable() {
								public void run() {
									mHandler.sendMessage(
											mHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_SEND_DATA, 5, -1));
								}
							};
							mHandler.postDelayed(r, aCommands.getTimeout());

						}

					}

				}
			};
		};
	}

	/**
	 * get latest Commands
	 * 
	 * @return
	 */
	public BTCommands getCurrentBTCommands() {
		if (aCommands.getFinished())
			return null;
		else
			return aCommands;
	}

	/**
	 * stop connection between bluetooth device and this software
	 */
	public void stop() {
		Log.i(TAG, "stop connection!");
		bStopWatchDog = true;
		bIsConnected = false;
		mHandler.removeCallbacksAndMessages(null);
		if (mBTChat != null) {
			mBTChat.stop();
			mBTChat = null;
		}
		if (aBTCommandsList != null && aBTCommandsList.size() > 0)
			aBTCommandsList.clear();
		aWatchDogThread = null;
		// aBTCommandsList = null;
		aBluetoothAdapter = null;
	}

	/**
	 * clear bluetooth command.
	 */
	public void clearBTCommand() {
		if (aCommands != null && aCommands.getCommandList() != null)
			aCommands.getCommandList().clear();
	}

	/**
	 * get connection status.
	 * 
	 * @return true: connected , false: disconnected.
	 */
	public boolean isConnected() {
		return bIsConnected;
	}

	/**
	 * send bluetooth command to device.
	 * 
	 * @param aCmds
	 * @see BTCommands
	 */
	public void sendBTCommand(BTCommands aCmds) {
		aCmds.setBTChat(mBTChat);
		aCmds.setCurrentConnection(this);
		aCmds.setBTInfo(aBTInfo);
		aBTCommandsList.offer(aCmds);
		if (bFirstBTCommands) {
			aCommands = aCmds;
			aCommands.setBTInfo(aBTInfo);
		}
		try {
			// if(aWatchDogThread!=null && !aWatchDogThread.isAlive() )
			// bStopWatchDog = false;
			aWatchDogThread.interrupt();// .run();
			// aWatchDogThread.start();
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	protected Handler mHandler = new Handler() {
		Message aMessage = null;
		Message aRawMessage = null;
		Bundle aBundle = null;
		Bundle aTmpBundle = null;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BlueToothGlobalSetting.MESSAGE_SERVICE_STOP:
				// System.out.println("MESSAGE SERVICE STOP");
				bIsConnected = false;
				if (mBTChat != null)
					mBTChat.stop();
				break;
			case BlueToothGlobalSetting.MESSAGE_SEND_DATA:
				switch (msg.arg1) {
				case 1:
					BTCommand aCmd1 = (BTCommand) aCommands.getCommandList().get(0);
					Log.i(TAG, "MESSAGE_SEND_COMMAND 1 = asc(" + new String(aCmd1.getCommandString()) + "),hex("
							+ HexAndStringConverter.convertHexByteToHexString(aCmd1.getCommandString()) + ")");

					if (mBTChat == null || mBTChat.getState() != mBTChat.STATE_CONNECTED) {
						aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
						aBundle = new Bundle();
						aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
						break;
					}

					try {
						if (aCmd1 instanceof ReadBTCommand) {
							mBTChat.read(((ReadBTCommand) aCmd1).getReaderChannelUUID());
						} else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd1.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd1.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd1.getCommandString(), aCmd1.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
								aMessage.setData(aBundle);
								aConnectionHandler.sendMessage(aMessage);
							}
						}
					} catch (NoWriterException e) {
						aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1,
								-1);
						aBundle = new Bundle();
						aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
					} catch (NoBTReaderException e) {
						aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_READER_UUID, 1,
								-1);
						aBundle = new Bundle();
						aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
					}
					if (iCommandSize == 1) {
						aCommands.getCommandList().clear();
					}
					break;

				case 2:
					try {
						BTCommand aCmd2 = (BTCommand) aCommands.getCommandList().get(1);
						Log.i(TAG, "MESSAGE_SEND_COMMAND 2 = asc(" + new String(aCmd2.getCommandString()) + "),hex("
								+ HexAndStringConverter.convertHexByteToHexString(aCmd2.getCommandString()) + ")");
						if (mBTChat == null || mBTChat.getState() != mBTChat.STATE_CONNECTED) {
							aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
							aBundle = new Bundle();
							aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
							aMessage.setData(aBundle);
							aConnectionHandler.sendMessage(aMessage);
							break;
						}

						if (aCmd2 instanceof ReadBTCommand) {
							mBTChat.read(((ReadBTCommand) aCmd2).getReaderChannelUUID());
						}  else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd2.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd2.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd2.getCommandString(), aCmd2.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
								aMessage.setData(aBundle);
								aConnectionHandler.sendMessage(aMessage);
							}
						}
						if (iCommandSize == 2) {
							aCommands.getCommandList().clear();
						}
					} catch (Exception ee) {
						Log.e(TAG, "some error occurr when send second command!" + ee.getMessage());
					}
					break;

				case 3:
					if (aCommands != null && aCommands.getCommandList().size() < 3)
						break;
					BTCommand aCmd3 = (BTCommand) aCommands.getCommandList().get(2);
					Log.i(TAG, "MESSAGE_SEND_COMMAND 3 = asc(" + new String(aCmd3.getCommandString()) + "),hex("
							+ HexAndStringConverter.convertHexByteToHexString(aCmd3.getCommandString()) + ")");
					if (mBTChat == null || mBTChat.getState() != mBTChat.STATE_CONNECTED) {
						aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
						aBundle = new Bundle();
						aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
						break;
					}

					try {
						if (aCmd3 instanceof ReadBTCommand) {
							mBTChat.read(((ReadBTCommand) aCmd3).getReaderChannelUUID());
						}  else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd3.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd3.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd3.getCommandString(), aCmd3.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
								aMessage.setData(aBundle);
								aConnectionHandler.sendMessage(aMessage);
							}
						}
					} catch (NoWriterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoBTReaderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (iCommandSize == 3) {
						aCommands.getCommandList().clear();
					}
					break;
				case 4:
					if (aCommands != null && aCommands.getCommandList().size() < 4)
						break;
					BTCommand aCmd4 = (BTCommand) aCommands.getCommandList().get(3);
					Log.i(TAG, "MESSAGE_SEND_COMMAND 4 = asc(" + new String(aCmd4.getCommandString()) + "),hex("
							+ HexAndStringConverter.convertHexByteToHexString(aCmd4.getCommandString()) + ")");
					if (mBTChat == null || mBTChat.getState() != IBlueToothChatService.STATE_CONNECTED) {
						aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
						aBundle = new Bundle();
						aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
						break;
					}

					try {
						if (aCmd4 instanceof ReadBTCommand) {
							mBTChat.read(((ReadBTCommand) aCmd4).getReaderChannelUUID());
						} else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd4.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd4.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd4.getCommandString(), aCmd4.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(BlueToothGlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
								aMessage.setData(aBundle);
								aConnectionHandler.sendMessage(aMessage);
							}
						}
					} catch (NoWriterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoBTReaderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (iCommandSize == 4) {
						aCommands.getCommandList().clear();
					}
					break;
				case 5:
					for (BTCommand cmd : aCommands.getOriginallCommandList()) {
						Log.i(TAG, "Cmd=" + cmd.getCommandString().toString() + "(Hex:"
								+ HexAndStringConverter.convertHexByteToHexString(cmd.getCommandString()) + ")");
					}
					try {
						if (!aCommands.getFinished()) {
							Log.i(TAG, "commands timeout! commands is " + aCommands.getClass());
							aCommands.handleTimeout();
						} else {
							Log.i(TAG, "Before timeout , command had finished!");
						}
					} catch (Exception ee) {
						Log.e(TAG, "sleep error", ee);
						aCommands.getCommandList().clear();
						aCommands.setFinished(true);
					}

					break;

				}
				break;
			case BlueToothGlobalSetting.MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BlueToothChatService.STATE_CONNECTED:
					if (bTriggerConnectedAndThenSendCommand) {
						bTriggerConnectedAndThenSendCommand = false;
					}
					aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTED, 1, -1);
					aBundle = new Bundle();
					aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
					aMessage.setData(aBundle);
					aConnectionHandler.sendMessageDelayed(aMessage, 800);
					bIsConnected = true;
					if (!aWatchDogThread.isAlive()) {
						Log.d(TAG, "watchdog thread start()");
						aWatchDogThread.start();
					}

					break;
				case BlueToothChatService.STATE_CONNECTING:
					break;
				case BlueToothChatService.STATE_LISTEN:
				case BlueToothChatService.STATE_NONE:
					break;
				case BlueToothChatService.STATE_LOST:
					bIsConnected = false;
					break;
				}
				break;
			case BlueToothGlobalSetting.MESSAGE_WRITE:

				break;
			case BlueToothGlobalSetting.MESSAGE_READ:
			//	Log.d(TAG, "data received =" + HexAndStringConverter.convertHexByteToHexString((byte)msg.arg1));
				
				if (aCommands != null && !aCommands.isDataTransferingCompleted()) {
					try {
						aCommands.getData(msg.arg1, msg.obj);
					} catch (Exception e) {
						e.printStackTrace();
						aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_UNKNOWN_EXCEPTION, msg.arg1, -1);
						aBundle = new Bundle();
						aBundle.putString(BlueToothGlobalSetting.BUNDLE_KEY_UNKNOWN_EXCEPTION_STRING, e.getMessage());
						aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
					}
				} else {
					aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_READ_BUT_NO_COMMNAND_HANDLE, msg.arg1, -1);
					aBundle = new Bundle();
					if(msg.obj!=null)
							aBundle.putString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING, msg.obj.toString());
					aMessage.setData(aBundle);
					aConnectionHandler.sendMessage(aMessage);
				}
				aRawMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_RAW_DATA, msg.arg1, -1);
				aBundle = new Bundle();
				if(msg.obj!=null)
				aBundle.putString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING, msg.obj.toString());
				aRawMessage.setData(aBundle);
				aConnectionHandler.sendMessage(aRawMessage);
				break;
			case BlueToothGlobalSetting.MESSAGE_DEVICE_NAME:
				break;
			case BlueToothGlobalSetting.MESSAGE_CONNECTION_FAIL:
				stop();
				Log.e(TAG, "MESSAGE_CONNECTION_FAIL!");
				if (aConnectionHandler == null) {
					Log.e(TAG, "No connectionHandler!");
					return;
				}
				aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessage(aMessage);
				break;
			case BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST:
				stop();
				Log.e(TAG, "MESSAGE_CONNECTION_LOST!");
				if (aConnectionHandler == null) {
					Log.e(TAG, "No connectionHandler!");
					return;
				}
				aMessage = aConnectionHandler.obtainMessage(BlueToothGlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessage(aMessage);

				break;
			case BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS:
				aTmpBundle = msg.getData();

				aMessage = aConnectionHandler
						.obtainMessage(BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aBundle.putString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
						aTmpBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessageDelayed(aMessage, 800);
				break;
			case BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL:
				aTmpBundle = msg.getData();

				aMessage = aConnectionHandler
						.obtainMessage(BlueToothGlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(BlueToothGlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aBundle.putString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
						aTmpBundle.getString(BlueToothGlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessageDelayed(aMessage, 800);
				break;
			case BlueToothGlobalSetting.MESSAGE_TOAST:
				break;
			}
		}
	};

}
