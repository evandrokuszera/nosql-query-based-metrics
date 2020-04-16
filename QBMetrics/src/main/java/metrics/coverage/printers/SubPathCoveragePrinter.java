/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics.coverage.printers;

import metrics.test_loader.AbstractTestInputLoader;

/**
 *
 * @author Evandro
 */
public class SubPathCoveragePrinter {
    private AbstractTestInputLoader loader;

    public SubPathCoveragePrinter(AbstractTestInputLoader loader) {
        this.loader = loader;
    }
    
    // 1. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA SUB PATH COVERAGE");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.subpath);
    }
    
    public void printCoverage(boolean by_query) {        
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA SUB PATH COVERAGE - BY Query");
        System.out.println("*********************************************************************************");        
        CoveragePrinter.printCoverage(loader, CoverageEnum.subpath, true);
    }
    
    // 2. /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printInvCoverage() {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA *INVERTED SUB PATH COVERAGE");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inv_subpath);
    }    
    
    public void printInvCoverage(boolean by_query) {
        System.out.println("*********************************************************************************");
        System.out.println("\t SCHEMA *INVERTED SUB PATH COVERAGE - BY Query");
        System.out.println("*********************************************************************************");
        CoveragePrinter.printCoverage(loader, CoverageEnum.inv_subpath, true);
    }
    
}
