import handler.FileHandler;
import handler.ProductionHandler;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        //读入产生式字符串，并做预处理以提供语法分析使用
        List originLines = FileHandler.readFileToList("./txt/test.txt");
        ProductionHandler productionHandler = new ProductionHandler();
        for(int i = 0; i < originLines.size(); i++){
            String currentOriginLine = (String) originLines.get(i);
            productionHandler.addLine(currentOriginLine);
        }
    }
}
