package Workers;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MapResults implements Serializable {

    private static final long serialVersionUID = -2723363029811966964L;
    private java.util.Map<String, List<CheckIn>> mapResults = null;

    public MapResults(Map<String, List<CheckIn>> mapResults) {
        //this.mapResults.putAll(mapResults);
        this.mapResults = mapResults;
    }

    public Map<String, List<CheckIn>> getMapResults() {
        return mapResults;
    }

    public void setMapResults(Map<String, List<CheckIn>> mapResults) {
        this.mapResults = mapResults;
    }
}
