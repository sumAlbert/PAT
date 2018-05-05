package handler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 98267
 */
public class FileHandler {

    public static List readFileToList(String fileURL){
        String temp;
        List<String> result = new ArrayList<>();
        File file = new File(fileURL);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            while((temp = reader.readLine()) != null){
                result.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static boolean writeListToFile(String fileURL, List outputList, String seperator) {
        boolean result = true;

        try {
            File file = new File(fileURL);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            FileWriter fileWritter = new FileWriter(file.getName(), false);

            for(int i = 0; i < outputList.size(); i++) {
                List data = (List) outputList.get(i);
                String outputStr = "";
                for(int j = 0; j < data.size(); j++) {
                    if( j != 0 ) {
                        outputStr = outputStr.concat(seperator);
                    }
                    String currentItem = (String) data.get(j);
                    if(currentItem.length() == 0) {
                        currentItem = "   ";
                    }
                    outputStr = outputStr.concat(currentItem);
                }
                outputStr = outputStr.concat("\n");
                fileWritter.write(outputStr);
            }
            fileWritter.close();

        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }
}
