package Workers;

import Request.Request;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Mapper implements MapWorkerInterfaceInterface {

    public static void main(String[] args) {
        Mapper mapper = new Mapper();
        mapper.initialize();

    }

    private ServerSocket providerSocket = null;
    private Socket connection = null;
    private Request request = null;
    private List<CheckIn> checkIns = null;


    public void initialize() {
        try {
            providerSocket = new ServerSocket(1403);
            checkIns = new ArrayList<CheckIn>();
            waitForTasksThread();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void waitForTasksThread() {
        try {
            while (true) {
                connection = this.providerSocket.accept();

                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                try {

                    this.request = (Request) in.readObject();

                    System.out.println("Received request:\n\t" + this.request.toString());
                    retrieveCheckins();
                    sendToReduce(map(checkIns));

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public java.util.Map<String, List<CheckIn>> map(List<CheckIn> checkIns) {

//        long counted = checkIns.parallelStream().count();
//        System.out.println("Counted: " + counted);

        Map<String, List<CheckIn>> theMap = checkIns.stream().parallel()
                .collect(Collectors.groupingBy(CheckIn::getPOI, Collectors.mapping(p -> p, Collectors.toList())));


        List<Map.Entry<String, List<CheckIn>>> sortedRes = theMap.entrySet().parallelStream()
                .sorted((s1, s2) -> Integer.compare(s2.getValue().size(), s1.getValue().size()))
                .collect(Collectors.toList());

        theMap.clear();
        System.out.println(sortedRes.size());
        List<Map.Entry<String, List<CheckIn>>> c = sortedRes.subList(0, 10);

        theMap = c.parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        for(String key : theMap.keySet()){
//            System.out.println("Key: "+key + " Value: " + theMap.get(key).size());
//        }

        for (Map.Entry<String, List<CheckIn>> entry : c) {
            System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue().size());
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
            dataSource.setServerName("83.212.117.76");


            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            String sql;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(request.getStartDate().getTime());
            sql = "SELECT * FROM ds_systems_2016.checkins WHERE " +
                    "latitude BETWEEN " + Double.toString(request.getLatitudeMin())
                    + " AND " + Double.toString(request.getLatitudeMax())
                    + " AND longitude BETWEEN " + Double.toString(request.getLongtitudeMin())
                    + " AND " + Double.toString(request.getLongtitudeMax())
                    + " AND time BETWEEN '" + sdf.format(request.getStartDate().getTime())
                    + "' AND '" + sdf.format(request.getEndDate().getTime()) + "'";
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);


            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String poi = rs.getString("POI");
                String poi_name = rs.getString("POI_name");
                String link = rs.getString("Photos");

                checkIns.add(new CheckIn(poi, poi_name, link));
//                distinct_POIs.add(id);
            }
            System.out.println(checkIns.size());


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void notifyMaster() {
    }


    public void sendToReduce(java.util.Map<String, List<CheckIn>> topResults) {

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        String message = null;
        try {
            requestSocket = new Socket("127.0.0.1", 1405);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(new MapResults(topResults));
            out.flush();
            checkIns.clear();

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
}
