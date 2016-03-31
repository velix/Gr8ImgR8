package Workers;

import Request.Request;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class Reducer implements ReduceWorkerInterface {

    public static void main(String[] args)
    {
        Reducer reducer = new Reducer();

        reducer.initialize();

        Map<String, List<CheckIn>> theGlobalMap = reducer.flatten(reducer.retrieveTheMaps());

        reducer.reduce(theGlobalMap);

    }


    private ServerSocket providerSocket = null;
    private Socket connection = null;
    private List<Map<String, List<CheckIn>>> theMaps = null;


    public void initialize()
    {
        try {
            providerSocket = new ServerSocket(1405);
            theMaps = new ArrayList<>();

            waitForTasksThread();

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    public void waitForTasksThread()
    {

        try {
            while (true) {
                connection = this.providerSocket.accept();

                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                try {
                    System.out.println("Received map from:" + connection.getInetAddress().getHostAddress());
                    theMaps.add(((MapResults)in.readObject()).getMapResults());
                    if(theMaps.size() >= 3){
                        break;
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void waitForMasterAck()
    {

    }

    public void reduce(Map<String, List<CheckIn>> theGlobalMap)
    {
        Collection<List<CheckIn>> checkIns = theGlobalMap.values();

        System.out.println("Reached reducer");

    }

    public void sendResults(Map<String, List<CheckIn>> theGlobalMap)
    {

    }

    public List<Map<String, List<CheckIn>>> retrieveTheMaps()
    {
        return theMaps;
    }

    private Map<String, List<CheckIn>> flatten(List<Map<String, List<CheckIn>>> theMaps)
    {
        Map<String, List<CheckIn>> flatenedMap = theMaps.get(0);

        for(int i = 1; i < theMaps.size(); i++)
        {
            flatenedMap.putAll(theMaps.get(i));
        }

        return flatenedMap;


    }
}
