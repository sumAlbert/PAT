package entity;

import javafx.util.Pair;

import java.util.*;

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

    public List print(Set stopSymbols, String endFlag, String againSymbol, String derivationSymbol) {
        List currentLineList;
        List<List> resultList = new ArrayList();

        //分析预测表首行
        currentLineList = new ArrayList();
        currentLineList.add("");
        Iterator stopIterator = stopSymbols.iterator();
        while (stopIterator.hasNext()) {
            String itemStr = (String) stopIterator.next();
            currentLineList.add(itemStr);
        }
        currentLineList.add(endFlag);
        resultList.add(currentLineList);

        //分析预测表其他行
        Iterator predictTableIterator = this.predictTable.entrySet().iterator();
        while (predictTableIterator.hasNext()) {//遍历预测分析表的每一行
            currentLineList = new ArrayList();
            Map.Entry predictTableEntry = (Map.Entry) predictTableIterator.next();
            String predictTableKey = (String) predictTableEntry.getKey();//该行的非终结符号
            Map predictTableValue = (Map) predictTableEntry.getValue();//该行的产生式列表
            currentLineList.add(predictTableKey);
            //遍历非终结符号
            stopIterator = stopSymbols.iterator();
            boolean lastItemFlag = false;
            boolean lastItemFlagEnd = false;
            while (stopIterator.hasNext() || lastItemFlag) {
                String itemStr;
                if(lastItemFlag) {//最后一个符号
                    lastItemFlagEnd = true;
                    lastItemFlag = false;
                    itemStr = endFlag;
                } else {
                    itemStr = (String) stopIterator.next();
                }
                String currentExpress = "";
                List currentItems = (List) predictTableValue.get(itemStr);//当前的终结符号
                if (currentItems != null) {
                    for (int j = 0; j < currentItems.size(); j++) {
                        if(j != 0) {
                            currentExpress = currentExpress.concat(againSymbol);
                        }
                        Pair currentPair = (Pair) currentItems.get(j);
                        String currentPairKey = (String) currentPair.getKey();
                        String[] currentPairValue = (String[]) currentPair.getValue();
                        currentExpress = currentExpress.concat(currentPairKey);
                        currentExpress = currentExpress.concat(derivationSymbol);
                        for(int m = 0; m < currentPairValue.length; m++) {
                            if(m != 0)
                                currentExpress = currentExpress.concat(" ");
                            currentExpress = currentExpress.concat(currentPairValue[m]);
                        }
                    }
                }

                //lastItemFlag 控制最后一个终结符号
                if(!stopIterator.hasNext() && !lastItemFlagEnd) {
                    lastItemFlag = true;
                }
                currentLineList.add(currentExpress);
            }
            resultList.add(currentLineList);
        }
        return resultList;
    }
}
