/*
 // Testes da class PathCoverage.
 //
 // Carrega os esquemas e consultas usados no experimento do artigo.
 // Duas opções de impressão: Schema x Consulta ou Consulta x Esquema (descomentar)
 // Path e InvertedPath são impressos.
*/
package metrics.coverage.printers;

import metrics.test_loader.AbstractTestInputLoader;

/**
 *
 * @author Evandro
 */
public class JoinCoveragePrinter {   
    private AbstractTestInputLoader loader;

    public JoinCoveragePrinter(AbstractTestInputLoader loader) {
        this.loader = loader;
    }
    
    // 1. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA JOIN COVERAGE");
        System.out.println("\t MinNbCol = Número mínimo de coleções para responder a consulta.");
        System.out.println("\t Coverage = Lista de possíveis junções de coleções para responder a consulta.");
        System.out.println("\t Query com 2 ou mais paths = a visualização está um pouco estranha, mas MinNbCol é a soma de todas as coleções (distintas) para responder todos os paths da query.");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.join_collection);
    }
    
//    public void printCoverage(boolean by_query) {        
//        System.out.println("*********************************************************************************");
//        System.out.println("\t SCHEMA JOIN COVERAGE by Query");
//        System.out.println("*********************************************************************************");        
//        CoveragePrinter.printCoverage(loader, CoverageEnum.path, true);
//    }
    
    // 2. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printInvCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t ATENÇÃO: NÃO FAZ DIFERENÇA ENTRE PATH E INVERTED PATH PARA A MÉTRICA JOIN. MOTIVO: SÓ MOSTRO O NÚMERO DE COLEÇÕES E NÃO O CAMINHO DO JOIN.");
        System.out.println("\t SCHEMA *Inverted JOIN COVERAGE");
        System.out.println("\t MinNbCol = Número mínimo de coleções para responder a consulta.");
        System.out.println("\t Coverage = Lista de possíveis junções de coleções para responder a consulta.");
        System.out.println("\t Query com 2 ou mais paths = a visualização está um pouco estranha, mas MinNbCol é a soma de todas as coleções (distintas) para responder todos os paths da query.");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inverted_join_collection);
    }    
    
//    public void printInvCoverage(boolean by_query) {
//        System.out.println("*********************************************************************************");
//        System.out.println("\t SCHEMA *INVERTED JOIN COVERAGE by Query");
//        System.out.println("*********************************************************************************");
//        CoveragePrinter.printCoverage(loader, CoverageEnum.inv_path, true);
//    }   
    
}
