/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics.coverage.printers;

/**
 *
 * @author Evandro
 */
public class PrinterUtils {
    
    public static String insertSpaceChars(String columnName, int maxLengthOfColumnName){        
        String spaces = "";
        if (columnName.length() < maxLengthOfColumnName){ // se o nome da coluna for menor tamanho mínimo de coluna...
            for (int i=0; i<(maxLengthOfColumnName - columnName.length()); i++){
                spaces += " "; // cria variável com número de espaços necessários...
            }
        }        
        return columnName + spaces;
    }
    
}
