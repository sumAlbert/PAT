package handler;

import entity.PredictTable;
import entity.Production;
import javafx.util.Pair;

import java.util.*;
import java.util.logging.Logger;

public class ProductionHandler {
    private Production production = new Production();
    private static final int ONLYONE = 1;
    private static final int TWOPARTS = 2;


    /**
     * 给当前的production添加一行产生式
     * @param line
     * @return
     */
    public boolean addLine(String line){
        boolean result = true;
        //原始行预处理到标准行
        line = line.replaceAll("^ +","");
        line = line.replaceAll(" +$","");
        line = line.replaceAll(" +"," ");

        //将标准行进行切割，并从中获取待处理的左右两部分
        String derivation = production.getSplitDerivation();
        String currentUnstop = production.getCurrentUnstop();
        Set unstopSymbols = production.getUnstopSymbols();
        Set stopSymbols = production.getStopSymbols();
        String[] lineArr = line.split(derivation);
        String leftPartStr = "";
        String rightPartStr = "";
        if(lineArr.length == TWOPARTS){
            leftPartStr = lineArr[0];
            rightPartStr = lineArr[1];
            leftPartStr = leftPartStr.replaceAll(" +","");
            String startSymbol = production.getStartSymbol();
            if(startSymbol.length() == 0){
                production.setStartSymbol(leftPartStr);
            }
            if(stopSymbols.contains(leftPartStr)){
                stopSymbols.remove(leftPartStr);
            }
            unstopSymbols.add(leftPartStr);
            production.setCurrentUnstop(leftPartStr);
            currentUnstop = leftPartStr;
        } else if (lineArr.length == ONLYONE){

            rightPartStr = lineArr[0];
        } else {
            result = false;
        }

        //根据待处理的左右两部分生成产生式集合
        if(currentUnstop.length() != 0){
            Map generator = production.getGenerators();
            String separator = production.getSplitSeparator();
            String[] rightPartArr = rightPartStr.split(separator);
            for(int i = 0; i < rightPartArr.length; i++){
                String currentRightItem = rightPartArr[i];
                currentRightItem = currentRightItem.replaceAll("^ +","");
                currentRightItem = currentRightItem.replaceAll(" +$","");
                String[] currentRightArr = currentRightItem.split(" ");
                for(int j = 0; j < currentRightArr.length;j++){
                    String currentRightArrItem = currentRightArr[j];
                    if(!unstopSymbols.contains(currentRightArrItem) && currentRightArrItem.length() != 0){
                        stopSymbols.add(currentRightArrItem);
                    }
                }
                List currentUnstopList = (List)generator.get(currentUnstop);//当前非终结符对应的表达式的集合
                if(currentUnstopList != null && currentRightItem.length() != 0){
                    currentUnstopList.add(currentRightItem);
                } else if(currentRightItem.length() != 0){
                    List<String> newUnstopList = new ArrayList();
                    newUnstopList.add(currentRightItem);
                    generator.put(currentUnstop,newUnstopList);
                }
            }
        } else {
            result = false;
        }
        return result;
    }
    /**
     * 消除左递归
     * @return
     */
    public boolean removeLeftRecursive(){
        boolean result = true;
        Set unstopSymbols = this.production.getUnstopSymbols();
        Map generatorsArr = this.production.getArrGenerators();
        Map generators = this.production.getGenerators();
        Iterator iterator = generatorsArr.entrySet().iterator();
        while (iterator.hasNext()){

            //将产生式右边表达式的集合划分为左递归和非左递归的两部分
            List<String[]> leftPart = new ArrayList<>();
            List<String[]> rightPart = new ArrayList<>();
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String)entry.getKey();
            List value = (List)entry.getValue();
            //遍历非终结符号每个右边的项
            for(int i = 0; i < value.size(); i++){
                String[] currentValues = (String[]) value.get(i);
                String firstSymbol = currentValues[0];
                if(firstSymbol.equals(key) && currentValues.length > 1){
                    leftPart.add(currentValues);
                } else if(firstSymbol.equals(key) && currentValues.length == 1){
                    result = false;
                    break;
                } else {
                    rightPart.add(currentValues);
                }
            }

            //生成新的非终结符，并消除左递归
            String nullSymbol = production.getNullSymbol();
            if(leftPart.size() != 0 && rightPart.size() != 0){
                String newKey = production.getNewUnstopSymbol(key);
                unstopSymbols.add(newKey);
                List<String> newValue1 = new ArrayList<>();//新非终结符的产生项
                List<String> newValue2 = new ArrayList<>();//旧非终结符的产生项
                for(int j = 0; j< leftPart.size(); j++){
                    String[] currentItem = leftPart.get(j);
                    String newItem = "";
                    for(int k = 1; k< currentItem.length; k++){
                        newItem = newItem.concat(currentItem[k]);
                        newItem = newItem.concat(" ");
                    }
                    newItem = newItem.concat(newKey);
                    newValue1.add(newItem);
                }
                newValue1.add(nullSymbol);
                for(int j = 0; j < rightPart.size(); j++){
                    String[] currentItem = rightPart.get(j);
                    String newItem = "";
                    for(int k = 0; k< currentItem.length; k++){
                        newItem = newItem.concat(currentItem[k]);
                        newItem = newItem.concat(" ");
                    }
                    newItem = newItem.concat(newKey);
                    newValue2.add(newItem);
                }
                generators.put(key,newValue2);
                generators.put(newKey,newValue1);
            } else if(leftPart.size() != 0 && rightPart.size() == 0){
                result = false;
                break;
            }
        }
        return result;
    }
    /**
     * 提取左因子
     * @return
     */
    public boolean promptCommonRefact(){
        boolean result = true;
        Map generatorsArr = this.production.getArrGenerators();
        String nullSymbol = this.production.getNullSymbol();//空字符
        List<Integer> diffInStrings = new ArrayList<>();//差值集合
        Map<String,List> newGenerators = new HashMap<>();//新的产生式集合
        Set unstopSymbols = this.production.getUnstopSymbols();//非终结符号
        List<String> unstopSymbolActive = new ArrayList<>();
        List<String[]> unstopItemsActive = new ArrayList<>();

        //开始遍历已有的产生式
        Iterator iterator = generatorsArr.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            List value = (List) entry.getValue();

            //对value进行排序，每一项都是一个String[],(冒泡排序)
            for(int i = 0; i < value.size() - 1; i++){
                for(int j = 0; j < value.size() - 1 - i; j++){
                    String[] currentStrings = (String[]) value.get(j);
                    String[] nextStrings = (String[]) value.get(j + 1);
                    if(compareForStrings(currentStrings,nextStrings) > 0){
                        value.set(j, nextStrings);
                        value.set(j + 1, currentStrings);
                    }
                }
            }

            //遍历value获取相邻两个差值
            for(int i = 0; i < value.size() - 1; i++){
                String[] currentStrings = (String[]) value.get(i);
                String[] nextStrings = (String[]) value.get(i + 1);
                diffInStrings.add(commonLenForStrings(currentStrings,nextStrings));
                unstopSymbolActive.add(key);
                unstopItemsActive.add(currentStrings);
            }
            diffInStrings.add(-1);
            unstopItemsActive.add((String[])value.get(value.size() -1));
            unstopSymbolActive.add(key);
        }

        //获取新的产生式集合
        int startIndex = 0;
        int endIndex = diffInStrings.size();
        while(startIndex != endIndex) {
            Integer currentDiff = diffInStrings.get(startIndex);
            int minDiff = Integer.MAX_VALUE;
            int setNum = 0;
            //取出可以合并的集合
            String currentKey = unstopSymbolActive.get(startIndex);
            while(currentDiff != null){
                if(currentDiff < minDiff && currentDiff > 0){
                    minDiff = currentDiff;
                }
                setNum++;
                startIndex++;
                if(currentDiff <= 0) {
                    break;
                }
                currentDiff = diffInStrings.get(startIndex);
            }
            //对集合进行合并(集合内只有一个值的时候，直接创建产生式放入集合)
            if(setNum > 1) {
                int setStart = startIndex - setNum;
                String newKey = this.production.getNewUnstopSymbol(currentKey);
                unstopSymbols.add(newKey);
                String newValue = "";
                for (int i = 0; i < minDiff; i++) {
                    newValue = newValue.concat(unstopItemsActive.get(setStart)[i]);
                    newValue = newValue.concat(" ");
                }
                newValue = newValue.concat(newKey);
                List generatorValue = newGenerators.get(currentKey);
                if (generatorValue == null) {
                    generatorValue = new ArrayList<String>();
                }
                generatorValue.add(newValue);
                newGenerators.put(currentKey, generatorValue);
                //产生新的活动集合
                for (int i = startIndex - setNum; i < startIndex; i++) {
                    String[] oldUnstopItemsActive = unstopItemsActive.get(i);
                    String[] newUnstopItemsActive;
                    if(oldUnstopItemsActive.length <= minDiff) {
                        newUnstopItemsActive = new String[1];
                        newUnstopItemsActive[0] = nullSymbol;
                    }
                    else {
                        newUnstopItemsActive = new String[oldUnstopItemsActive.length - minDiff];
                        for(int j = 0; j < oldUnstopItemsActive.length - minDiff; j++) {
                            newUnstopItemsActive[j] = oldUnstopItemsActive[minDiff + j];
                        }
                    }
                    unstopItemsActive.add(newUnstopItemsActive);
                    diffInStrings.add(diffInStrings.get(i) - minDiff);
                    unstopSymbolActive.add(newKey);
                    endIndex++;
                }
            } else {//集合内只有一个值的时候
                int setStart = startIndex - setNum;
                String newValue = "";
                for (int i = 0; i < unstopItemsActive.get(setStart).length; i++) {
                    if(i != 0) {
                        newValue = newValue.concat(" ");
                    }
                    newValue = newValue.concat(unstopItemsActive.get(setStart)[i]);
                }
                List generatorValue = newGenerators.get(currentKey);
                if (generatorValue == null) {
                    generatorValue = new ArrayList<String>();
                }
                generatorValue.add(newValue);
                newGenerators.put(currentKey, generatorValue);
            }
        }
        this.production.setGenerators(newGenerators);
        return result;
    }
    /**
     * 获取左集合
     * @return
     */
    public boolean obtainFirstSet() {
        boolean result = true;
        Map<String,Set> firstSets = new HashMap();
        Set stopSymbols= this.production.getStopSymbols();
        Set upStopSymbols = this.production.getUnstopSymbols();
        String nullSymbol = this.production.getNullSymbol();
        //处理空字
        recursiveFirstSet(nullSymbol,firstSets);

        //处理终结符号
        Iterator iterator = stopSymbols.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            recursiveFirstSet(key,firstSets);
        }

        //处理非终结符号
        iterator = upStopSymbols.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            recursiveFirstSet(key,firstSets);
        }
        this.production.setFirstSet(firstSets);
        return result;
    }
    /**
     * 获取followSet
     * @return
     */
    public boolean obtainFollowSet() {
        boolean result = true;
        Map<String,Set> followSets = new HashMap();
        Set avoidOutStack = new HashSet();
        Set unstopSymbols = this.production.getUnstopSymbols();
        Iterator iterator = unstopSymbols.iterator();
        while (iterator.hasNext()) {
            String entry = (String) iterator.next();
            recursiveFollowSet(entry,followSets,avoidOutStack);
        }
        this.production.setFollowSet(followSets);
        return result;
    }
    /**
     * 获取分析预测表
     * @return
     */
    public boolean obtainPredictTable() {
        boolean result = true;
        Map generatorArr = this.production.getArrGenerators();
        String nullSymbol = this.production.getNullSymbol();
        String endFlag = this.production.getEndFlag();
        PredictTable predictTable = new PredictTable();
        Set stopSymbols = this.production.getStopSymbols();
        Map firstSets = this.production.getFirstSet();
        Map followSets = this.production.getFollowSet();
        Iterator iterator = generatorArr.entrySet().iterator();
        while(iterator.hasNext()) {//遍历每个非终结符的产生式
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            List value = (List) entry.getValue();
            for(int i = 0; i < value.size(); i++) {//遍历每个产生式
                boolean followFlag = false;
                String[] items = (String[]) value.get(i);
                for(int j = 0; j < items.length; j++) {
                    String currentItem = items[j];
                    Set firstSet = (Set) firstSets.get(currentItem);
                    Iterator iteratorSet = firstSet.iterator();
                    while(iteratorSet.hasNext()) {//遍历first集合
                        String key2 = (String) iteratorSet.next();
                        if(stopSymbols.contains(key2)) {
                            predictTable.insert(key,key2,key,items);
                        }
                        if(key2.equals(nullSymbol)) {//存在'空'
                            followFlag = true;
                        }
                    }
                    //基于follow
                    if(followFlag) {
                        Set followSet = (Set) followSets.get(key);
                        iteratorSet = followSet.iterator();
                        while (iteratorSet.hasNext()) {
                            String key2 = (String) iteratorSet.next();
                            if(stopSymbols.contains(key2) || key2.equals(endFlag)) {
                                predictTable.insert(key,key2,key,items);
                            }
                        }
                        continue;
                    }
                    break;
                }
            }
        }
        this.production.setPredictTable(predictTable);
        return result;
    }

    /**
     * 递归获取followSet
     * @param key
     * @param followSets
     * @return
     */
    private Set recursiveFollowSet(String key,Map followSets,Set avoidOutStack) {
        if(avoidOutStack.contains(key)) {
            return new HashSet();
        }
        avoidOutStack.add(key);
        Set result = new HashSet();
        String startSymbol = this.production.getStartSymbol();
        String nullSymbol = this.production.getNullSymbol();
        String endFlag = this.production.getEndFlag();
        Map firstSets = this.production.getFirstSet();

        //插入结束标志
        if(key.equals(startSymbol)) {
            Set followSet = (Set) followSets.get(key);
            if(followSet == null) {
                followSet = new HashSet();
                followSet.add(endFlag);
                followSets.put(key, followSet);
            }
        }

        //基于倒排索引后的产生式生成相应的followSet
        Map reverseIndex = this.production.getReverseIndex();
        List currentList = (List) reverseIndex.get(key);
        if(currentList != null) {
            for(int i = 0; i < currentList.size(); i++) {
                Pair currentGenerator = (Pair) currentList.get(i);
                String[] currentValue = (String[]) currentGenerator.getValue();
                String currentKey = (String) currentGenerator.getKey();
                for(int j = 0; j < currentValue.length; j++) {
                    String currentItem = currentValue[j];
                    if(currentItem.equals(key)) {
                        Set currentSet = (Set) followSets.get(currentItem);
                        if(currentSet == null) {
                            currentSet = new HashSet();
                            followSets.put(currentItem,currentSet);
                        }

                        //后面仍然存在值
                        if(j != currentValue.length - 1) {
                            //基于first
                            String nextItem = currentValue[j+1];
                            Set waitAddSet = (Set) firstSets.get(nextItem);
                            if(waitAddSet.contains(nullSymbol) && !currentItem.equals(currentKey)) {
                                Set waitAddSet2 = recursiveFollowSet(currentKey,followSets,avoidOutStack);
                                currentSet.addAll(waitAddSet2);
                            }
                            Set tempSet = new HashSet();
                            tempSet.addAll(waitAddSet);
                            tempSet.remove(nullSymbol);
                            currentSet.addAll(tempSet);
                        }

                        //后面不存在值
                        if(j == currentValue.length - 1 && !currentItem.equals(currentKey)) {
                            Set waitAddSet2 = recursiveFollowSet(currentKey,followSets,avoidOutStack);
                            currentSet.addAll(waitAddSet2);
                        }
                    }
                }
            }
        }
        result = (Set) followSets.get(key);
        avoidOutStack.remove(key);
        return result;
    }
    /**
     * 递归申城firstSet
     * @param key
     * @param firstSets
     * @return
     */
    private Set recursiveFirstSet(String key,Map firstSets) {
        Set result = null;
        Set stopSymbols= this.production.getStopSymbols();
        String nullSymbol = this.production.getNullSymbol();
        if(stopSymbols.contains(key) || key.equals(nullSymbol)) {
            Set keySet = (Set) firstSets.get(key);
            if(keySet == null) {
                keySet = new HashSet();
                firstSets.put(key,keySet);
            }
            keySet.add(key);
            result = keySet;
        }
        else {
            Map generatorsArr = this.production.getArrGenerators();
            List value = (List) generatorsArr.get(key);
            Set keySet = (Set) firstSets.get(key);
            if(keySet == null) {
                keySet = new HashSet();
                firstSets.put(key,keySet);
            }
            //遍历产生式右边的每一项
            for(int i = 0; i < value.size(); i++) {
                String[] currentValue = (String[]) value.get(i);
                String currentStr = currentValue[0];
                Set currentSet = recursiveFirstSet(currentStr,firstSets);
                keySet.addAll(currentSet);
                for(int j = 1; j < currentValue.length; j++) {
                    if(currentSet.contains(nullSymbol)) {
                        currentStr = currentValue[j];
                        currentSet = recursiveFirstSet(currentStr,firstSets);
                        keySet.addAll(currentSet);
                    }
                    else {
                        break;
                    }
                }
            }
            result = keySet;
        }
        return result;
    }
    /**
     * 比较两个字符串数组
     * @param strs1
     * @param strs2
     * @return
     */
    private int compareForStrings(String[] strs1,String[] strs2){
        int result = 0;
        int i = 0;
        while(result == 0){
            if(i >= strs1.length && i < strs2.length){
                result = -1;
            } else if(i < strs1.length && i >= strs2.length){
                result = 1;
            } else if(i < strs1.length && i < strs2.length){
                String str1 = strs1[i];
                String str2 = strs2[i];
                result = str1.compareTo(str2);
            } else {
                break;
            }
            i++;
        }
        return result;
    }
    /**
     * 获取String[]共有长度
     * @param strs1
     * @param strs2
     * @return
     */
    private Integer commonLenForStrings(String[] strs1,String[] strs2){
        int i = 0;
        while(i < strs1.length && i < strs2.length) {
            if(!strs1[i].equals(strs2[i])){
                break;
            }
            i++;
        }
        Integer result = new Integer(i);
        return result;
    }



    //getter && setter
    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }
}
