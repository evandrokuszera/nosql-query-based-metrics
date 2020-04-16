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
public class EdgeCoveragePrinter {  
    private AbstractTestInputLoader loader;

    public EdgeCoveragePrinter(AbstractTestInputLoader loader) {
        this.loader = loader;
    }
    
    public void printDirectEdgeCoverage(){
        System.out.println("*********************************************************************************");
        System.out.println("\t DIRECT EDGE COVERAGE");
        System.out.println("\t Desconsiderar MaxDepth");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.direct_edge);
    }
    
    public void printDirectEdgeCoverage(boolean by_query){
        System.out.println("*********************************************************************************");
        System.out.println("\t DIRECT EDGE COVERAGE, By Query");
        System.out.println("\t Desconsiderar MaxDepth");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.direct_edge, by_query);
    }
    
    public void printInvertedEdgeCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t INVERTED EDGE COVERAGE");
        System.out.println("\t Desconsiderar MaxDepth");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inverted_edge);
    }
    
    public void printInvertedEdgeCoverage(boolean by_query) {
        System.out.println("*********************************************************************************");
        System.out.println("\t INVERTED EDGE COVERAGE, By Query");
        System.out.println("\t Desconsiderar MaxDepth");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inverted_edge, by_query);
    }
    
    public void printAllEdgeCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t ALL EDGE COVERAGE");
        System.out.println("\t Desconsiderar MaxDepth");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.alltypes_edge);
    }
    
    public void printAllEdgeCoverage(boolean by_query) {
        System.out.println("*********************************************************************************");
        System.out.println("\t ALL EDGE COVERAGE, By Query");
        System.out.println("\t Desconsiderar MaxDepth");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.alltypes_edge, by_query);
    }
    
}
