package Workers;

import java.util.List;
import java.util.Map;


public interface ReduceWorkerInterface extends  WorkerInterface {

    void waitForMasterAck();
    void reduce(Map<String, List<CheckIn>> theGlobalMap);
    void sendResults(Map<String,List<CheckIn>> finalMap);
}
