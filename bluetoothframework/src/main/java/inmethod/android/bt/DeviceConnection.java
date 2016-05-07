package inmethod.android.bt;

import java.util.LinkedList;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import inmethod.android.bt.classic.ClassicChatService;
import inmethod.android.bt.command.BTCommand;
import inmethod.android.bt.command.BTCommands;
import inmethod.android.bt.command.BTReadCommand;
import inmethod.android.bt.exception.NoBTReaderException;
import inmethod.android.bt.exception.NoWriterException;
import inmethod.android.bt.handler.ConnectionCallbackHandler;
import inmethod.android.bt.interfaces.IChatService;
import inmethod.commons.util.HexAndStringConverter;

public class DeviceConnection {

	public final String TAG = GlobalSetting.TAG + "/" + getClass().getSimpleName();

	protected static final boolean D = true;

	private boolean bIsConnected = false;
	private BTCommands aCommands = null;
	private int iCommandSize = 0;
	private boolean bTriggerConnectedAndThenSendCommand = false;
	private boolean bStopWatchDog = false;
	private boolean bFirstBTCommands = true;
	private BTInfo aBTInfo = null;
	private BluetoothAdapter aBluetoothAdapter = null;
	private IChatService mBTChat = null;
	private LinkedList<BTCommands> aBTCommandsList = null;
	private ConnectionCallbackHandler aConnectionHandler = null;
	private Thread aWatchDogThread = null;
	private Context aContext = null;

	private DeviceConnection() {
	}

	/**
	 * 
	 * @param aBtInfo
	 * @param context
	 * @param aBTChat
	 * @param aCallBackHandler
	 */
	public DeviceConnection(BTInfo aBtInfo, Context context, IChatService aBTChat,
							ConnectionCallbackHandler aCallBackHandler) {
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
	public void setCallBackHandler(ConnectionCallbackHandler aHandler) {
		aConnectionHandler = aHandler;
	}

	/**
	 * get Connection call back handler.
	 */
	public ConnectionCallbackHandler getCallBackHandler() {
		return aConnectionHandler ;
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
		bStopWatchDog = false;
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

					if (!bFirstBTCommands && !aCommands.isFinished()) {
					//	Log.d(TAG, "current running cmds=" + aCommands.toString());
						try {
							sleep(800);
						} catch (InterruptedException e) {
						}
					}

					if (mBTChat != null && mBTChat.getState() != ClassicChatService.STATE_CONNECTED) {
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

					if (isConnected() && aBTCommandsList.size() > 0 && (aCommands.isFinished() || bFirstBTCommands)) {
						if (aCommands.isFinished()) {
							Log.i(TAG, "remove timeout thread because command is finished!");
							mHandler.removeCallbacksAndMessages(null);
						}
						bFirstBTCommands = false;
						aCommands = aBTCommandsList.poll();
						aCommands.setBTInfo(aBTInfo);
						iCommandSize = aCommands.getCommandList().size();
						if (aCommands.getCommandList().size() >= 1) {

							try {
								mHandler.sendMessage(mHandler.obtainMessage(GlobalSetting.MESSAGE_SEND_DATA, 1, -1));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						if (aCommands.getCommandList().size() >= 2) {
							try {
								mHandler.sendMessageDelayed(
										mHandler.obtainMessage(GlobalSetting.MESSAGE_SEND_DATA, 2, -1),
										((BTCommand) aCommands.getCommandList().get(0)).getDelayTime());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						if (aCommands.getCommandList().size() >= 3) {
							try {
								mHandler.sendMessageDelayed(
										mHandler.obtainMessage(GlobalSetting.MESSAGE_SEND_DATA, 3, -1),
										((BTCommand) aCommands.getCommandList().get(0)).getDelayTime()
												+ ((BTCommand) aCommands.getCommandList().get(1)).getDelayTime());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (aCommands.getCommandList().size() >= 4) {
							try {
								mHandler.sendMessageDelayed(
										mHandler.obtainMessage(GlobalSetting.MESSAGE_SEND_DATA, 4, -1),
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
											mHandler.obtainMessage(GlobalSetting.MESSAGE_SEND_DATA, 5, -1));
								}
							};
							mHandler.postDelayed(r, aCommands.getTimeout());

						}

					}

				}
				if(bStopWatchDog){
					Log.d(TAG,"stop watchdog");
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
		if (aCommands.isFinished())
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
		bFirstBTCommands = true;
		mHandler.removeCallbacksAndMessages(null);
		if (mBTChat != null) {
			mBTChat.stop();
			//mBTChat = null;
		}
		if (aBTCommandsList != null && aBTCommandsList.size() > 0)
			aBTCommandsList.clear();
		//aWatchDogThread = null;
		// aBTCommandsList = null;
		//aBluetoothAdapter = null;
	}

	/**
	 * clear bluetooth command.
	 */
	public void clearBTCommand() {
		if (aCommands != null && aCommands.getCommandList() != null)
			aCommands.getCommandList().clear();
	}

	/**
	 * force current BTCommands timeout Immediately
	 */
	public void forceBTCommandsTimeout(){
		if( aCommands!=null && !aCommands.isFinished()) {
			mHandler.removeCallbacksAndMessages(null);
			try {
				aCommands.handleTimeout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	/**
	 *  remove CallBack and Messages
	 */
	public void removeCallbacksAndMessages(){

			mHandler.removeCallbacksAndMessages(null);

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
			case GlobalSetting.MESSAGE_SERVICE_STOP:
				// System.out.println("MESSAGE SERVICE STOP");
				bIsConnected = false;
				if (mBTChat != null)
					mBTChat.stop();
				break;
			case GlobalSetting.MESSAGE_SEND_DATA:
				switch (msg.arg1) {
				case 1:
					BTCommand aCmd1 = (BTCommand) aCommands.getCommandList().get(0);
					Log.i(TAG, "MESSAGE_SEND_COMMAND 1 = asc(" + new String(aCmd1.getCommandString()) + "),hex("
							+ HexAndStringConverter.convertHexByteToHexString(aCmd1.getCommandString()) + ")");

					if (mBTChat == null || mBTChat.getState() != mBTChat.STATE_CONNECTED) {
						aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
						aBundle = new Bundle();
						aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
						break;
					}

					try {
						if (aCmd1 instanceof BTReadCommand) {
							mBTChat.read(((BTReadCommand) aCmd1).getReaderChannelUUID());
						} else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd1.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd1.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd1.getCommandString(), aCmd1.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(GlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
								aMessage.setData(aBundle);
								aConnectionHandler.sendMessage(aMessage);
							}
						}
					} catch (NoWriterException e) {
						aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1,
								-1);
						aBundle = new Bundle();
						aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
					} catch (NoBTReaderException e) {
						aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_EXCEPTION_NO_READER_UUID, 1,
								-1);
						aBundle = new Bundle();
						aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
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
							aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
							aBundle = new Bundle();
							aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
							aMessage.setData(aBundle);
							aConnectionHandler.sendMessage(aMessage);
							break;
						}

						if (aCmd2 instanceof BTReadCommand) {
							mBTChat.read(((BTReadCommand) aCmd2).getReaderChannelUUID());
						}  else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd2.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd2.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd2.getCommandString(), aCmd2.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(GlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
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
						aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
						aBundle = new Bundle();
						aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
						break;
					}

					try {
						if (aCmd3 instanceof BTReadCommand) {
							mBTChat.read(((BTReadCommand) aCmd3).getReaderChannelUUID());
						}  else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd3.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd3.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd3.getCommandString(), aCmd3.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(GlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
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
					if (mBTChat == null || mBTChat.getState() != IChatService.STATE_CONNECTED) {
						aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
						aBundle = new Bundle();
						aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
						break;
					}

					try {
						if (aCmd4 instanceof BTReadCommand) {
							mBTChat.read(((BTReadCommand) aCmd4).getReaderChannelUUID());
						} else if (aBTInfo.getDeviceBlueToothType() != BTInfo.DEVICE_TYPE_LE) {
							mBTChat.write(aCmd4.getCommandString(), null);
						} else if (aBTInfo.getDeviceBlueToothType() == BTInfo.DEVICE_TYPE_LE) {
							if (aCmd4.getWriterChannelUUID() != null) {
								mBTChat.write(aCmd4.getCommandString(), aCmd4.getWriterChannelUUID());
							} else {
								aMessage = aConnectionHandler
										.obtainMessage(GlobalSetting.MESSAGE_EXCEPTION_NO_WRITER_UUID, 1, -1);
								aBundle = new Bundle();
								aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
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
						if (!aCommands.isFinished()) {
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
			case GlobalSetting.MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case ClassicChatService.STATE_CONNECTED:
					if (bTriggerConnectedAndThenSendCommand) {
						bTriggerConnectedAndThenSendCommand = false;
					}
					aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTED, 1, -1);
					aBundle = new Bundle();
					aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
					aMessage.setData(aBundle);
					aConnectionHandler.sendMessageDelayed(aMessage, 50);
					bIsConnected = true;
					if (!aWatchDogThread.isAlive()) {
						Log.d(TAG, "watchdog thread start()");
						aWatchDogThread.start();
					}

					break;
				case ClassicChatService.STATE_CONNECTING:
					break;
				case ClassicChatService.STATE_LISTEN:
				case ClassicChatService.STATE_NONE:
					break;
				case ClassicChatService.STATE_LOST:
					bIsConnected = false;
					break;
				}
				break;
			case GlobalSetting.MESSAGE_WRITE:

				break;
			case GlobalSetting.MESSAGE_READ:
			//	Log.d(TAG, "data received =" + HexAndStringConverter.convertHexByteToHexString((byte)msg.arg1));
				
				if (aCommands != null && !aCommands.isFinished()) {
					try {
						aCommands.getData((byte)msg.arg1, msg.obj);
					} catch (Exception e) {
						e.printStackTrace();
						aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_UNKNOWN_EXCEPTION, msg.arg1, -1);
						aBundle = new Bundle();
						aBundle.putString(GlobalSetting.BUNDLE_KEY_UNKNOWN_EXCEPTION_STRING, e.getMessage());
						aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
						aMessage.setData(aBundle);
						aConnectionHandler.sendMessage(aMessage);
					}
				} else {
					aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_READ_BUT_NO_COMMNAND_HANDLE, msg.arg1, -1);
					aBundle = new Bundle();
					if(msg.obj!=null)
							aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING, msg.obj.toString());
					aMessage.setData(aBundle);
					aConnectionHandler.sendMessage(aMessage);
				}
				aRawMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_RAW_DATA, msg.arg1, -1);
				aBundle = new Bundle();
				if(msg.obj!=null)
				aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING, msg.obj.toString());
				aRawMessage.setData(aBundle);
				aConnectionHandler.sendMessage(aRawMessage);
				break;
			case GlobalSetting.MESSAGE_DEVICE_NAME:
				break;
			case GlobalSetting.MESSAGE_CONNECTION_FAIL:
				stop();
				Log.e(TAG, "MESSAGE_CONNECTION_FAIL!");
				if (aConnectionHandler == null) {
					Log.e(TAG, "No connectionHandler!");
					return;
				}
				aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessage(aMessage);
				break;
			case GlobalSetting.MESSAGE_CONNECTION_LOST:
				stop();
				Log.e(TAG, "MESSAGE_CONNECTION_LOST!");
				if (aConnectionHandler == null) {
					Log.e(TAG, "No connectionHandler!");
					return;
				}
				aMessage = aConnectionHandler.obtainMessage(GlobalSetting.MESSAGE_CONNECTION_LOST, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessage(aMessage);

				break;
			case GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS:
				aTmpBundle = msg.getData();

				aMessage = aConnectionHandler
						.obtainMessage(GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_SUCCESS, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
						aTmpBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessageDelayed(aMessage, 500);
				break;
			case GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL:
				aTmpBundle = msg.getData();

				aMessage = aConnectionHandler
						.obtainMessage(GlobalSetting.MESSAGE_ENABLE_NOTIFICATION_OR_INDICATOR_FAIL, 1, -1);
				aBundle = new Bundle();
				aBundle.putParcelable(GlobalSetting.BUNDLE_KEY_BLUETOOTH_INFO, aBTInfo);
				aBundle.putString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING,
						aTmpBundle.getString(GlobalSetting.BUNDLE_KEY_READER_UUID_STRING));
				aMessage.setData(aBundle);
				aConnectionHandler.sendMessageDelayed(aMessage, 500);
				break;
			case GlobalSetting.MESSAGE_TOAST:
				break;
			}
		}
	};

}
