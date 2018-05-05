package entity;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictTable {
    public Map<String,Map> predictTable = new HashMap<>();

    public Map<String, Map> getPredictTable() {
        return predictTable;
    }

    public void setPredictTable(Map<String, Map> predictTable) {
        this.predictTable = predictTable;
    }

    public void insert(String key1, String key2,String unstopSymbol,String[] items) {
        Map<String,List> predictTable2 = this.predictTable.get(key1);
        if(predictTable2 == null) {
            predictTable2 = new HashMap<>();
            this.predictTable.put(key1,predictTable2);
        }
        List predictList = predictTable2.get(key2);
        if(predictList == null) {
            predictList = new ArrayList();
            predictTable2.put(key2,predictList);
        }
        Pair pair = new Pair(unstopSymbol,items);
        predictList.add(pair);
    }
}
