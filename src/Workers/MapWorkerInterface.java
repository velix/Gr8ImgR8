package Workers;

import java.util.List;
import java.util.Map;

/**
 * Created by Velix on 27/3/2016.
 */
public interface MapWorkerInterface extends Worker {

    Map<String, List<CheckIn>> map(List<CheckIn> data);
    void notifyMaster();
    void sendToReduce(java.util.Map<Integer,Object> topResults);
}
