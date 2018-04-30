package handler;

import entity.Production;

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
        boolean result = false;
        Map generatorsArr = this.production.getArrGenerators();
        String separateSymbol= this.production.getSeparateSymbol();
        List<Integer> diffInStrings = new ArrayList<>();
        Map<String,List> newGenerators = new HashMap<>();
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
        System.out.print("test");
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
