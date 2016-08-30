package inmethod.android.bt.simulation;

/**
 * Created by william on 2016/8/24.
 */
public class ResponsedData {

    protected byte[] byteResponsedData = null;

    public ResponsedData(){}

    /**
     * default responsed data
     * @param data
     */
    public  ResponsedData(byte[] data){
        byteResponsedData = data;
    }
    /**
     * This medhod will get BTCommands Data , we can overwrite this method and  process those data  from BTCommands.
      <pre>
             you can get default responsed data by calling  getResponsedData()
     or  access attribute   "byteResponsedData"  directly
      </pre>
     * @param iCmdOrder  BTCommands can send multi BTCommand , iCmdOrder show command order (1,2,3...)
     * @param data
     */
    public void handleBTCommandsData(int iCmdOrder, byte[] data){

    }

    /**
     *  when BTCommands sent , overwrite this method to simulate responsed data from remote bluetooth device.
     */
    public byte[] getResponsedData(){
        if(byteResponsedData!=null )
        return byteResponsedData;
        else return new byte[]{'n','o',' ','r','e','s','p','o','s','e','d',' ','d','a','t','a'};
    }

    public void setResponsedData(byte[] data){
        byteResponsedData = data;
    }

}
