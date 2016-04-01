package Workers;

import Request.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


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

       Map<String, POI_record> poiRecMap = new HashMap<>();
    		   
       theGlobalMap.entrySet().stream().parallel()
   			 			.forEach(p -> poiRecMap.put(p.getKey(), createRecord(p)) );

       List<POI_record> sortedRes = poiRecMap.entrySet().parallelStream()
               .sorted((s1, s2) -> Integer.compare(s2.getValue().getCountOfChecIns(), s1.getValue().getCountOfChecIns() ))
               .map(p -> p.getValue())
               .collect(Collectors.toList());
       
       List<POI_record> finalList = sortedRes.subList(0, 10);

	   for(POI_record rec : finalList )
	   {
		   System.out.println("REC: " + rec.toString());   
	   }
        
        
        
    	
    }
    
    private POI_record createRecord(Map.Entry<String,List<CheckIn>> entry)
    {
    	
    	List<String> photos = unique_photos(entry.getValue());
    	
    	POI_record rec = new POI_record (entry.getValue().get(0).getPOI(), entry.getValue().get(0).getLatitude()
    			,entry.getValue().get(0).getLongitude() 
    			, photos
    			, entry.getValue().size());
    	
    	return rec;
    	
    }
    
    
    private List<String> unique_photos(List<CheckIn> checkIns)
    {
    	Set<String> unique_photos = new TreeSet<>();
    	
    	checkIns.stream().parallel()
    				.forEach(p -> unique_photos.add(p.getLink()));
    				
    	return new ArrayList<String>(unique_photos);
    	
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
        Map<String, List<CheckIn>> flattenedMap = theMaps.get(0);

        for(int i = 1; i < theMaps.size(); i++)
        {
            flattenedMap.putAll(theMaps.get(i));
        }

        return flattenedMap;

    }
}
