package Workers;

import SharedClasses.Request;

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
        int localPort = -1;
        String reducerIP = null;
        int reducerPort = -1;
        String masterIP = null;
        int masterPort = -1;
        switch (args.length){
            case 3:
                localPort = Integer.valueOf(args[0]);
                reducerIP = args[1];
                reducerPort = Integer.valueOf(args[2]);
                break;
            default:
                localPort = 1501;
                reducerIP = "127.0.0.1";
                reducerPort = 1505;
                masterIP = "192.168.1.7";
                masterPort = 8080;
                System.out.println("Using default configuration.");
        }
        while(true) {
            Mapper mapper = new Mapper(reducerIP, reducerPort, masterIP, masterPort, localPort);
            mapper.initialize();
        }
    }


    private ServerSocket providerSocket = null;
    private Socket connection = null;
    private Request request = null;
    private List<CheckIn> checkIns = null;
    private String reducerIP;
    private int reducerPort;
    private String masterIP;
    private int masterPort;
    private int localPort;

    public Mapper(String reducerIP, int reducerPort,String  masterIP, int masterPort, int localPort) {
        this.reducerIP = reducerIP;
        this.reducerPort = reducerPort;
        this.localPort = localPort;
        this.masterIP = masterIP;
        this.masterPort = masterPort;
    }

    public void initialize() {
        try {
            providerSocket = new ServerSocket(localPort);
            checkIns = new ArrayList<CheckIn>();
            waitForTasksThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitForTasksThread() {
        try {
            connection = this.providerSocket.accept();

            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

            try {

                this.request = (Request) in.readObject();

                System.out.println("Received request from Client: "+ connection.getInetAddress().getHostAddress() );
                retrieveCheckins();
                sendToReduce(map(checkIns));
                notifyMaster();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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


        //String dbURL = "jdbc:mysql://83.212.117.76:3306?user=omada41&password=omada41db";
        String dbURL = "jdbc:mysql://localhost:3306?user=root&password=4267";
        String dbClass = "com.mysql.jdbc.Driver";

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sql = "SELECT * FROM nyc.checkins WHERE "
                    + "latitude BETWEEN " + Double.toString(request.getLatitudeMin())
                    + " AND " + Double.toString(request.getLatitudeMax())
                    + " AND longitude BETWEEN " + Double.toString(request.getLongtitudeMin())
                    + " AND " + Double.toString(request.getLongtitudeMax())
                    + " AND time BETWEEN '" + request.getStartDate()
                    + "' AND '" + request.getEndDate() + "'";
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
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        try {
            requestSocket = new Socket(this.masterIP, this.masterPort);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeBoolean(true);
            out.flush();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown master!");
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


    public void sendToReduce(java.util.Map<String, List<CheckIn>> topResults) {

        Socket requestSocket = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        String message = null;
        boolean ack = false;
        try {
            requestSocket = new Socket(this.reducerIP, this.reducerPort);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeObject(new MapResults(topResults));
            out.flush();

            out.writeInt(this.request.getK());
            out.flush();

            checkIns.clear();

            ack = in.readBoolean();
            if(ack){
                System.out.println("Data delivered to reducer");
            }
            assert ack;

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown reducer!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (requestSocket != null) {
                    requestSocket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
