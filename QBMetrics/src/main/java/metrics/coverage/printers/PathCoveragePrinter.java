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
public class PathCoveragePrinter {   
    private AbstractTestInputLoader loader;

    public PathCoveragePrinter(AbstractTestInputLoader loader) {
        this.loader = loader;
    }
    
    // 1. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA PATH COVERAGE");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.path);
    }
    
    public void printCoverage(boolean by_query) {        
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA PATH COVERAGE by Query");
        System.out.println("*********************************************************************************");        
        CoveragePrinter.printCoverage(loader, CoverageEnum.path, true);
    }
    
    // 2. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printInvCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA *INVERTED PATH COVERAGE");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inv_path);
    }    
    
    public void printInvCoverage(boolean by_query) {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA *INVERTED PATH COVERAGE by Query");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inv_path, true);
    }   
    
}
