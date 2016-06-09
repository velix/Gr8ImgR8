package Workers;

import SharedClasses.POI_record;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class Reducer implements ReduceWorkerInterface {

    public static void main(String[] args) throws InterruptedException {
        int localPortResults = -1;
        int localPortAck = -1;
        String masterIP = null;
        int masterPort = -1;
        switch (args.length){
            case 3:
                localPortResults = Integer.valueOf(args[0]);
                masterIP = args[1];
                masterPort = Integer.valueOf(args[2]);
                break;
            default:
                localPortResults = 1505;
                localPortAck = 1506;
                masterIP = "192.168.1.7";
                masterPort = 8080;
                System.out.println("Using default configuration.");
        }
        while(true) {
            Reducer reducer = new Reducer(masterIP, masterPort, localPortResults, localPortAck);

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    reducer.initialize();
                } });
            t1.start();

            reducer.waitForMasterAck();
            t1.interrupt();

            Map<String, List<CheckIn>> theGlobalMap = reducer.flatten(reducer.theMaps);

            reducer.reduce(theGlobalMap);

            reducer.sendResults();
            try {
                reducer.providerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private ServerSocket providerSocket = null;
    private Socket connection = null;
    private List<Map<String, List<CheckIn>>> theMaps = null;
    private List<POI_record> finalList = null;
    private int k;
    private String clientIP;
    private int clientPort;
    private int localPortResults;
    private int localPortAck;
    private boolean flag;

    public Reducer(String clientIP, int clientPort, int localPortResults, int localPortAck) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.localPortResults = localPortResults;
        this.localPortAck = localPortAck;
        this.flag = true;
    }

    public void initialize() {
        try {
            providerSocket = new ServerSocket(localPortResults);
            providerSocket.setReceiveBufferSize(3);
            theMaps = new ArrayList<>();
            waitForTasksThread();

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    public void waitForTasksThread() {

        try {
            while (flag) {
                connection = this.providerSocket.accept();

                ObjectOutputStream out = new ObjectOutputStream((connection.getOutputStream()));
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                System.out.println("Waiting for tasks..");
                try {
                    System.out.print("Received Results from Mapper: " + connection.getInetAddress().getHostAddress());

                    Map<String, List<CheckIn>> current_map = ((MapResults) in.readObject()).getMapResults();
                    System.out.println(" top " + current_map.size() + " POIs.");

                    this.k = in.readInt();
                    theMaps.add(current_map);
                    out.writeBoolean(true);
                    out.flush();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Socket closed..");
        }
        finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void waitForMasterAck() {

        ServerSocket providerSocket = null;
        Socket connection = null;
        ObjectInputStream in = null;
        boolean message = false;
        try {
            providerSocket = new ServerSocket(localPortAck);
            connection = providerSocket.accept();
            in = new ObjectInputStream(connection.getInputStream());
            message = in.readBoolean();
            if(!message){
                System.err.println("Error: Wrong ACK from master");
            }else{
                System.out.println("Received Ack from master : " + connection.getInetAddress().getHostAddress());
                flag = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (providerSocket != null) {
                    providerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void reduce(Map<String, List<CheckIn>> theGlobalMap) {
        System.out.println("Reduce..");
       Map<String, POI_record> poiRecMap = new HashMap<>();
    		   
       theGlobalMap.entrySet().stream().parallel()
   			 			.forEach(p -> poiRecMap.put(p.getKey(), createRecord(p)) );

       List<POI_record> sortedRes = poiRecMap.entrySet().parallelStream()
               .sorted((s1, s2) -> Integer.compare(s2.getValue().getCountOfChecIns(), s1.getValue().getCountOfChecIns()))
               .map(p -> p.getValue())
               .collect(Collectors.toList());

        if(sortedRes.size() >= this.k){
            this.finalList = new ArrayList(sortedRes.subList(0, this.k));
        }else{
            this.finalList = sortedRes;
        }

    }
    
    private POI_record createRecord(Map.Entry<String,List<CheckIn>> entry)
    {
    	
    	List<String> photos = unique_photos(entry.getValue());
    	
    	POI_record rec = new POI_record (entry.getValue().get(0).getPOI(), entry.getValue().get(0).getPOI_name()
                , entry.getValue().get(0).getLatitude()
    			, entry.getValue().get(0).getLongitude()
    			, photos
    			, entry.getValue().size());
    	
    	return rec;
    	
    }
    
    
    private List<String> unique_photos(List<CheckIn> checkIns)
    {
    	Set<String> unique_photos = new HashSet<>();
    	checkIns.stream().parallel()
    				.forEach(p -> unique_photos.add(p.getLink()));

    	return new ArrayList<String>(unique_photos);
    	
    }
    
    
    public void sendResults()
    {

        System.out.println("Sending Results to Client..");
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        String message = null;
        try {
            requestSocket = new Socket(clientIP, clientPort);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(this.finalList);
            out.flush();

            this.finalList.clear();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public List<Map<String, List<CheckIn>>> retrieveTheMaps()
    {
        return theMaps;
    }

    private Map<String, List<CheckIn>> flatten(List<Map<String, List<CheckIn>>> theMaps)
    {
        Map<String, List<CheckIn>> flattenedMap = theMaps.get(0);

        for(int i = 1; i < theMaps.size(); i++)
        {
            flattenedMap.putAll(theMaps.get(i));
        }

        return flattenedMap;

    }
}
