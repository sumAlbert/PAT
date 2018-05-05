package entity;

import javafx.util.Pair;

import java.util.*;

public class Production {
    private Set<String> unstopSymbols = new HashSet<>();
    private Set<String> stopSymbols = new HashSet<>();
    private Map<String,List> generators = new HashMap<>();
    private Map<String,Set> firstSet = new HashMap<>();
    private Map<String,Set> followSet = new HashMap<>();
    private PredictTable predictTable = new PredictTable();
    private String derivationSymbol = "->";
    private String separateSymbol = "|";
    private String startSymbol = "";
    private String currentUnstop = "";
    private String nullSymbol = "e";
    private String endFlag = "$";
    private String againSymbol = ",";

    public Map<String, List> getGenerators() {
        return generators;
    }
    public Set<String> getStopSymbols() {
        return stopSymbols;
    }
    public Set<String> getUnstopSymbols() {
        return unstopSymbols;
    }
    public String getDerivationSymbol() {
        return derivationSymbol;
    }
    public String getSeparateSymbol() {
        return separateSymbol;
    }
    public String getStartSymbol() {
        return startSymbol;
    }
    public String getCurrentUnstop() {
        return currentUnstop;
    }
    public String getNullSymbol() {
        return nullSymbol;
    }
    public Map<String,Set> getFirstSet() {
        return firstSet;
    }
    public String getEndFlag() {
        return endFlag;
    }
    public Map<String, Set> getFollowSet() {
        return followSet;
    }
    public PredictTable getPredictTable() {
        return predictTable;
    }
    public String getAgainSymbol() {
        return againSymbol;
    }

    public void setDerivationSymbol(String derivationSymbol) {
        this.derivationSymbol = derivationSymbol;
    }
    public void setGenerators(Map<String, List> generators) {
        this.generators = generators;
    }
    public void setSeparateSymbol(String separateSymbol) {
        this.separateSymbol = separateSymbol;
    }
    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
    }
    public void setStopSymbols(Set<String> stopSymbols) {
        this.stopSymbols = stopSymbols;
    }
    public void setUnstopSymbols(Set<String> unstopSymbols) {
        this.unstopSymbols = unstopSymbols;
    }
    public void setCurrentUnstop(String currentUnstop) {
        this.currentUnstop = currentUnstop;
    }
    public void setNullSymbol(String nullSymbol) {
        this.nullSymbol = nullSymbol;
    }
    public void setFirstSet(Map<String,Set> firstSet) {
        this.firstSet = firstSet;
    }
    public void setEndFlag(String endFlag) {
        this.endFlag = endFlag;
    }
    public void setFollowSet(Map<String, Set> followSet) {
        this.followSet = followSet;
    }
    public void setPredictTable(PredictTable predictTable) {
        this.predictTable = predictTable;
    }
    public void setAgainSymbol(String againSymbol) {
        this.againSymbol = againSymbol;
    }

    //将分隔号和推到号转换成可以split的格式
    public String getSplitDerivation (){
        String result = this.derivationSymbol;
        if(result.equals(".")){
            result = "\\.";
        } else if(result.equals("|")){
            result =  "\\|";
        } else if(result.equals("\\")){
            result = "\\\\";
        }
        return result;
    }
    public String getSplitSeparator () {
        String result = this.separateSymbol;
        if(result.equals(".")){
            result = "\\.";
        } else if(result.equals("|")){
            result =  "\\|";
        } else if(result.equals("\\")){
            result = "\\\\";
        }
        return result;
    }

    //根据产生式获取倒排索引的map
    public Map getReverseIndex() {
        Map<String,List> result = new HashMap();
        Map generatorArr = getArrGenerators();
        Iterator iterator = generatorArr.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            List value = (List) entry.getValue();
            for(int i = 0; i < value.size(); i++) {
                String[] currentValue = (String[]) value.get(i);
                for(int j = 0; j < currentValue.length; j++) {
                    String currentItem = currentValue[j];
                    if(unstopSymbols.contains(currentItem)) {
                        List currentKeyList = result.get(currentItem);
                        if(currentKeyList == null) {
                            currentKeyList = new ArrayList();
                            result.put(currentItem,currentKeyList);
                        }
                        Pair pair = new Pair(key,currentValue);
                        currentKeyList.add(pair);
                    }
                }
            }
        }
        return result;
    }
    //将产生式变成数组形式
    public Map getArrGenerators (){
        Map<String,List> result = new HashMap<>();
        Iterator iterator = this.generators.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            List value = (List) entry.getValue();
            List<String[]> newValue = new ArrayList<>();
            for(int i = 0; i < value.size(); i++){
                String[] currentSubValues = ((String) value.get(i)).split(" ");
                newValue.add(currentSubValues);
            }
            result.put(key,newValue);
        }
        return result;
    }
    //得到不重复的新的非终结符号
    public String getNewUnstopSymbol (String oldSymbol){
        String newSymbol = oldSymbol;
        while(stopSymbols.contains(newSymbol)||(unstopSymbols.contains(newSymbol))){
            newSymbol = newSymbol.concat("'");
        }
        return newSymbol;
    }
}