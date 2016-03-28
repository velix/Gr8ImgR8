package Workers;

import java.util.List;

public class Record {

    private List<CheckIn> recordList;

    public Record() {
        this.recordList = null;
    }

    public List<CheckIn> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<CheckIn> recordList) {
        this.recordList = recordList;
    }

    public void add(CheckIn c){
        this.recordList.add(c);
    }
    public int count(){
        return recordList.size();
    }
}
