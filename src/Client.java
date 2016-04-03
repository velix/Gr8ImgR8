import Request.Request;
import Workers.MapResults;
import Workers.POI_record;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class Client extends Thread {

    Request request;
    String address;
    int port, status;

    public Client(Request request, String address, int port) {
        this.request = request;
        this.address = address;
        this.port = port;
        this.status = 0;
    }

    public static void main(String[] args){
        List<String> addresses = Arrays.asList("127.0.0.1", "127.0.0.1", "127.0.0.1");
        List<Integer> ports = Arrays.asList(1401, 1402, 1403);
        //Request r = new Request(40.721854, -74.011651, 40.746398, -73.933891,
        //        new GregorianCalendar(2012,4,4,10,25,0), new GregorianCalendar(2013,2,11,21,45,0));

        //Request r = new Request(40.724310, -74.008070, 40.925788, -73.475606,
        //        new GregorianCalendar(2012,03,03,18,00,00), new GregorianCalendar(2013,01,16,02,36,00));
        //TODO: Replace GregorialCalendar with a simple String. Strange behaviour concerning month. Java's problem.

        Request r = new Request(40.755931, -74.004868, 40.793370, -73.935860,
                new GregorianCalendar(2012,3,4-1,18,0,0), new GregorianCalendar(2013,2-1,16,2,36,0));

        List<Request> rl = splitRequest(r, 3);
        Client c1 = new Client(rl.get(0), addresses.get(0), ports.get(0));
        Client c2 = new Client(rl.get(1), addresses.get(1), ports.get(1));
        Client c3 = new Client(rl.get(2), addresses.get(2), ports.get(2));
        c1.start();
        c2.start();
        c3.start();

        collectDataFromReducers();

        try {
            c1.join();
            c2.join();
            c3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
        if (c1.status==1 && c2.status==1 && c3.status==1){
            System.out.println("Mappers are done");
            //ackToReducers("127.0.0.1", 1404);
        }else{
            System.out.println(c1.status);
            System.out.println(c2.status);
            System.out.println(c3.status);
        }*/

    }

    public void run() {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String message = null;
        try {
            requestSocket = new Socket(InetAddress.getByName(address), port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(this.request);
            out.flush();

            /*
            message = (String) in.readObject();
            System.out.println(message);
            if(message.equalsIgnoreCase("Hello!")){
                System.out.println(port);
                this.status = 1;
            }*/

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
//-------------------------------------------------------------------------------------------------------------------
/*
    public void waitForMappers(){
        ServerSocket providerSocket = null;
        Socket connection = null;
        String message = null;
        int[] ackFlags = {0, 0, 0};
        try{
            providerSocket = new ServerSocket(4321);

            while(true){
                connection = providerSocket.accept();

                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                do{
                    try{
                        message = (String) in.readObject();
                        System.out.println(connection.getInetAddress().getHostAddress() + ">" + message);
                    }catch(ClassNotFoundException classnot){
                        System.err.println("Data received in unknown format");
                    }
                }while(!message.equals("Done"));
                in.close();
                out.close();
                connection.close();
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally{
            try{
                providerSocket.close();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
*/
    public static List<Request> splitRequest(Request r, int nOfSubspaces) {
        List<Double> a = new ArrayList<Double>();
        for (int i = 0; i <= nOfSubspaces; i++) {
            double t = i / (double) nOfSubspaces;
            a.add(r.getLongtitudeMax() * t  + r.getLongtitudeMin() * (1 - t));
        }
        List<Request> rl = new ArrayList<Request>();

        for (int i = 0; i < nOfSubspaces; i++) {
            rl.add(new Request(r.getLatitudeMin(), a.get(i), r.getLatitudeMax(), a.get(i+1), r.getStartDate(), r.getEndDate()));
        }

        return rl;
    }

/*
    public static void ackToReducers(String reducerAddress, int reducerPort){
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String message = null;
        try {
            requestSocket = new Socket(InetAddress.getByName(reducerAddress), reducerPort);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeUTF("Ready");
            out.flush();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
*/

    public static void collectDataFromReducers(){
        ServerSocket providerSocket = null;
        Socket connection = null;
        String message = null;
        try {
                providerSocket = new ServerSocket(1400);
                connection = providerSocket.accept();

                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                try {
                    System.out.println("Received Results from Reducer: " + connection.getInetAddress().getHostAddress());
                    List<POI_record> finalList = ((List<POI_record>)in.readObject());

                    for(POI_record rec : finalList )
                    {
                        System.out.println(finalList.indexOf(rec) + 1 + ":\t" + rec.toString());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
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
        return;
    }
}
