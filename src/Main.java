import entity.Production;
import handler.FileHandler;
import handler.ProductionHandler;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        boolean resultFlag = true;

        //读入产生式字符串，并做预处理以提供语法分析使用
        List originLines = FileHandler.readFileToList("./txt/test2.txt");
        ProductionHandler productionHandler = new ProductionHandler();
        for(int i = 0; i < originLines.size(); i++){
            String currentOriginLine = (String) originLines.get(i);
            resultFlag = productionHandler.addLine(currentOriginLine);
        }
        if(!resultFlag){
            System.out.println("词法分析阶段存在错误");
            return ;
        }

        //消除左递归
        resultFlag = productionHandler.removeLeftRecursive();
        if(!resultFlag){
            System.out.println("消除左递归阶段存在错误");
            return ;
        }

        //提取公因子
        resultFlag = productionHandler.promptCommonRefact();
        if(!resultFlag){
            System.out.println("提取公因子阶段存在错误");
            return ;
        }

        //获取firstSet集合
        resultFlag = productionHandler.obtainFirstSet();
        if(!resultFlag){
            System.out.println("获取first集合阶段存在错误");
            return ;
        }

        //获取followSet集合
        resultFlag = productionHandler.obtainFollowSet();
        if(!resultFlag){
            System.out.println("获取follow集合阶段存在错误");
            return ;
        }

        //获取分析预测表
        resultFlag = productionHandler.obtainPredictTable();
        if(!resultFlag){
            System.out.println("获取分析预测表存在错误");
            return ;
        }

        //获取分析预测表的字符串
        List resultStrs = productionHandler.printPredictTable();

        //打印列表
        FileHandler.writeListToFile("./txt/result1.txt",resultStrs,productionHandler.getProduction().getSeparateSymbol());
    }
}
