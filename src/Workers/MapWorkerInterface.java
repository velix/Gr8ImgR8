package Workers;

import java.util.List;

/**
 * Created by Velix on 27/3/2016.
 */
public interface MapWorkerInterface extends Worker {

    java.util.Map<Integer,Object> map(List<CheckIn> data);
    void notifyMaster();
    void sendToReduce(java.util.Map<Integer,Object> topResults);
}
