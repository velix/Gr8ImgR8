package Workers;

import Coordinates.Coordinates;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Velix on 27/3/2016.
 */
public class Mapper implements MapWorkerInterface
{

    public static void main(String[] args)
    {
        Mapper mapper = new Mapper();
        mapper.retrieveCheckins();
    }
    private ServerSocket providerSocket = null;
    private Socket connection = null;
    private Coordinates coordinates = null;
//    TODO: this comes from client
    private static int K = 10;

    public void initialize()
    {
        try {
            providerSocket = new ServerSocket(4320);

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
                    this.coordinates = (Coordinates) in.readObject();
//                    TODO: send thia to a method to extract and retrieve from db
                }
                catch(ClassNotFoundException e)
                {
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

    public  java.util.Map<String, List<CheckIn>> map(List<CheckIn> checkIns)
    {

//        long counted = checkIns.parallelStream().count();
//        System.out.println("Counted: " + counted);

        Map<String, List<CheckIn>> theMap = checkIns.stream().parallel()
                .collect(Collectors.groupingBy(CheckIn::getPOI, Collectors.mapping(p -> p, Collectors.toList())));


        List<Map.Entry<String, List<CheckIn>>> sortedRes =  theMap.entrySet().parallelStream()
                .sorted((s1, s2) ->Integer.compare(s2.getValue().size(), s1.getValue().size()))
                .collect(Collectors.toList());

        theMap.clear();

        List<Map.Entry<String, List<CheckIn>>> c = sortedRes.subList(0,K);

        theMap = c.parallelStream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        for(String key : theMap.keySet()){
//            System.out.println("Key: "+key + " Value: " + theMap.get(key).size());
//        }

        for(Map.Entry<String, List<CheckIn>> entry : c)
        {
            System.out.println("Key: "+ entry.getKey() + " Value: " + entry.getValue().size());
        }

        return theMap;
    }


    public void retrieveCheckins() {

        //Database Connection and Retrieval
        Connection conn = null;
        Statement stmt = null;
        try {

            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser("omada41");
            dataSource.setPassword("omada41db");
            dataSource.setServerName("195.251.252.98");


            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM ds_systems_2016.checkins WHERE longitude>-74.015306 AND longitude<-74.010424 AND latitude>40.709436 AND latitude<40.712559";
            ResultSet rs = stmt.executeQuery(sql);

            List<CheckIn> checkinsList = new ArrayList<>();

            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String poi = rs.getString("POI");
                String poi_name = rs.getString("POI_name");
                String link = rs.getString("Photos");

                checkinsList.add(new CheckIn(poi, poi_name, link));
//                distinct_POIs.add(id);
            }

            this.map(checkinsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void notifyMaster() {}
    public void sendToReduce(java.util.Map<Integer,Object> topResults){}



}
