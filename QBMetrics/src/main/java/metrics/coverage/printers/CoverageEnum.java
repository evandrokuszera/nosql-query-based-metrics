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
public enum CoverageEnum {
    path(1), subpath(2), indpath(3), 
    inv_path(4), inv_subpath(5), inv_indpath(6), 
    direct_edge(7), inverted_edge(8), alltypes_edge(9), 
    join_collection(10), inverted_join_collection(11);
    
    private final int opcao;
    
    CoverageEnum (int op){
        opcao = op;
    }
    
    public int getValor(){
        return opcao;
    }
}
