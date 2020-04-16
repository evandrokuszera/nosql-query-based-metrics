/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics.coverage.printers;

import metrics.Metrics;
import metrics.test_loader.AbstractTestInputLoader;

/**
 *
 * @author Evandro
 */
public class IndirectPathCoveragePrinter {
    private AbstractTestInputLoader loader;

    public IndirectPathCoveragePrinter(AbstractTestInputLoader loader) {
        this.loader = loader;
    }
    
    // 1. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA INDIRECT PATH COVERAGE");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.indpath);
    }
    
    public void printCoverage(boolean by_query) {        
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA INDIRECT PATH COVERAGE - BY Query");
        System.out.println("*********************************************************************************");        
        CoveragePrinter.printCoverage(loader, CoverageEnum.indpath, true);
    }
    
    // 2. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printInvCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA *INVERTED INDIRECT PATH COVERAGE");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inv_indpath);
    }    
    
    public void printInvCoverage(boolean by_query) {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA *INVERTED INDIREC PATH COVERAGE - BY Query");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inv_indpath, true);
    }
}
