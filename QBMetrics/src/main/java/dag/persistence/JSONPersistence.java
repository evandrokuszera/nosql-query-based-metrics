/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.persistence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class JSONPersistence {
    
     // Carrega arquivo JSON do disco
    public static JSONObject loadJSONfromFile(String filePath){      
        String jsonText = "";
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);
            
            String linha = br.readLine();
            while (linha != null){
                jsonText += linha;
                linha = br.readLine();                
            }
            fr.close();            
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        JSONObject json = new JSONObject(jsonText);
        
        return json;        
    }       
        
    // Salva JSON em disco.
    public static void saveJSONtoFile(JSONObject obj, String filePath){             
        // Save JSON in filePath...
        FileWriter fw;
        try {
            fw = new FileWriter(filePath);
            obj.write(fw);
            fw.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
        
        //System.out.println(obj);
    }
}
