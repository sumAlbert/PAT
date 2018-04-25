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
                    if(!unstopSymbols.contains(currentRightArrItem)){
                        stopSymbols.add(currentRightArrItem);
                    }
                }
                List currentUnstopList = (List)generator.get(currentUnstop);
                if(currentUnstopList != null){
                    currentUnstopList.add(currentRightItem);
                } else {
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

    public boolean removeLeftRecursive(){
        boolean result = true;
        Map generatorsArr = this.production.getArrGenerators();
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
            if(leftPart.size() != 0 && rightPart.size() != 0){
                List<String[]> newValue1 = new ArrayList<>();
                List<String[]> newValue2 = new ArrayList<>();

            } else if(leftPart.size() != 0 && rightPart.size() == 0){
                result = false;
                break;
            }

        }
        return result;
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }
}
