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

}
