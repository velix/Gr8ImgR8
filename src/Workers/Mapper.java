package Workers;

import Request.Request;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Mapper implements MapWorkerInterfaceInterface {

    public static void main(String[] args) {
        int port = -1;
        String reducerIP = null;
        int reducerPort = -1;
        switch (args.length){
            case 1:
                port = Integer.valueOf(args[0]);
                break;
            case 3:
                port = Integer.valueOf(args[0]);
                reducerIP = args[1];
                reducerPort = Integer.valueOf(args[2]);
                break;
            default:
                port = 1503;
                reducerIP = "127.0.0.1";
                reducerPort = 1505;
                System.out.println("Using default configuration.");
        }
        Mapper mapper = new Mapper(reducerIP, reducerPort);
        mapper.initialize(port);
    }


    private ServerSocket providerSocket = null;
    private Socket connection = null;
    private Request request = null;
    private List<CheckIn> checkIns = null;
    private String reducerIP;
    private int reducerPort;

    public Mapper(String reducerIP, int reducerPort) {
        this.reducerIP = reducerIP;
        this.reducerPort = reducerPort;
    }

    public void initialize(int port) {
        try {
            providerSocket = new ServerSocket(port);
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

                    System.out.println("Received request from Client: "+ connection.getInetAddress().getHostAddress() );
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


        Map<String, List<CheckIn>> theMap = checkIns.stream().parallel()
                .collect(Collectors.groupingBy(CheckIn::getPOI, Collectors.mapping(p -> p, Collectors.toList())));


        List<Map.Entry<String, List<CheckIn>>> sortedRes = theMap.entrySet().parallelStream()
                .sorted((s1, s2) -> Integer.compare(s2.getValue().size(), s1.getValue().size()))
                .collect(Collectors.toList());

        theMap.clear();
        System.out.println("There are " + sortedRes.size() + " distinct POIs");
        List<Map.Entry<String, List<CheckIn>>> topSortedRes = null;
        if(sortedRes.size() >= request.getK()){
            topSortedRes = sortedRes.subList(0, request.getK());
        }else{
            topSortedRes = sortedRes;
        }


        theMap = topSortedRes.parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        for (Map.Entry<String, List<CheckIn>> entry : topSortedRes) {
            System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue().size());
        }

        return theMap;
    }


    public void retrieveCheckins() {

        //Database Connection and Retrieval
        Connection conn = null;
        Statement stmt = null;


        String dbURL = "jdbc:mysql://83.212.117.76:3306?user=omada41&password=omada41db";
        String dbClass = "com.mysql.jdbc.Driver";

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sql = "SELECT * FROM ds_systems_2016.checkins WHERE "
                    + "latitude BETWEEN " + Double.toString(request.getLatitudeMin())
                    + " AND " + Double.toString(request.getLatitudeMax())
                    + " AND longitude BETWEEN " + Double.toString(request.getLongtitudeMin())
                    + " AND " + Double.toString(request.getLongtitudeMax())
                    + " AND time BETWEEN '" + sdf.format(request.getStartDate().getTime())
                    + "' AND '" + sdf.format(request.getEndDate().getTime()) + "'";
            System.out.println(sql);


            Class.forName(dbClass);
            conn = DriverManager.getConnection(dbURL);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String poi = rs.getString("POI");
                String poi_name = rs.getString("POI_name");
                String link = rs.getString("Photos");
                String lat = rs.getString("latitude");
                String lon = rs.getString("longitude");

                checkIns.add(new CheckIn(lat, lon, poi, poi_name, link));
//                distinct_POIs.add(id);
            }
            conn.close();
            System.out.println("In total there are " + checkIns.size() + " checkins");


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
            requestSocket = new Socket(this.reducerIP, this.reducerPort);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(new MapResults(topResults));
            out.flush();

            out.writeInt(this.request.getK());
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
