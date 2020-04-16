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
public class CoveragePrinter {
    private AbstractTestInputLoader loader;

    public CoveragePrinter(AbstractTestInputLoader loader) {
        this.loader = loader;        
    }
    
    public EdgeCoveragePrinter edge(){
        return new EdgeCoveragePrinter(loader);
    }
    
    public IndirectPathCoveragePrinter indirectPath(){
        return new IndirectPathCoveragePrinter(loader);
    }
            
    public PathCoveragePrinter path(){
        return new PathCoveragePrinter(loader);
    }
        
     public SubPathCoveragePrinter subpath(){
        return new SubPathCoveragePrinter(loader);
    } 
     
    public JoinCoveragePrinter join(){
        return new JoinCoveragePrinter(loader);
    }
     
    // Métodos padrão para impressão da cobertura das métricas:
    // - 1 path, 2 subpath, 3 indirectpath
    // - 4 *path, 5 *subpath, 6 *indirectpath
    // - 7 direct edge, 8 inverted edge, 9 all edge    
    
    // Imprime as métricas por SCHEMA
    //
    public static void printCoverage(AbstractTestInputLoader loader, CoverageEnum coverageOption) {
        for (int s = 0; s<loader.getSchemas().size(); s++){
            Metrics metrics = new Metrics(loader.getSchemas().get(s));
            System.out.println("Schema: "+ loader.getSchemas().get(s).getName());
            
            if (coverageOption == CoverageEnum.join_collection || coverageOption == CoverageEnum.inverted_join_collection)
                System.out.println("Query\tMinNbCol\tCoverage (Join Possibilities between Collections)");
            else
                System.out.println("Query\tMaxCov\tMaxDepth\tCoverage by Collection");
            
            for (int i=0; i<loader.getQueries().size(); i++){  
                System.out.print((i+1));
                switch (coverageOption) {
                    case path:
                        System.out.print("\t" + metrics.queryPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryPath().getSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case subpath:
                        System.out.print("\t" + metrics.querySubPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.querySubPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.querySubPath().getSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case indpath:
                        System.out.print("\t" + metrics.queryIndirectPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryIndirectPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryIndirectPath().getSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case inv_path:
                        System.out.print("\t" + metrics.queryPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryPath().getInvSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case inv_subpath:
                        System.out.print("\t" + metrics.querySubPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.querySubPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.querySubPath().getInvSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case inv_indpath:
                        System.out.print("\t" + metrics.queryIndirectPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryIndirectPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryIndirectPath().getInvSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case direct_edge:
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryEdge().getSchemaDirectEdgeCoverage(loader.getQueries().get(i)));
                        break;
                    case inverted_edge:
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaInvertedEdgeCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaInvertedEdgeCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryEdge().getSchemaInvertedEdgeCoverage(loader.getQueries().get(i)));
                        break;
                    case alltypes_edge:
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaAllEdgeCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaAllEdgeCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryEdge().getSchemaAllEdgeCoverage(loader.getQueries().get(i)));
                        break;
                        
                    case join_collection:
                        System.out.print("\t" + metrics.queryJoin().getMaxSchemaCoverage(loader.getQueries().get(i), false).getNumberOfCollection());
                        System.out.println("\t" + metrics.queryJoin().getSchemaCoverage(loader.getQueries().get(i), false));
                        break;
                        
                    case inverted_join_collection:
                        System.out.print("\t" + metrics.queryJoin().getMaxSchemaCoverage(loader.getQueries().get(i), true).getNumberOfCollection());
                        System.out.println("\t" + metrics.queryJoin().getSchemaCoverage(loader.getQueries().get(i), true));
                        break;
                }
            }
            System.out.println("");
        }
    }
    
    // Imprime as métricas por QUERY
    //
    public static void printCoverage(AbstractTestInputLoader loader, CoverageEnum coverageOption, boolean by_query) {        
        for (int i=0; i<loader.getQueries().size(); i++){            
            System.out.println("Query "+(i+1)+": " + new Metrics(null).path().getPaths(loader.getQueries().get(i)));
            System.out.println("Schema\tMaxCov\tMaxDepth\tCoverage by Collection");
            
            for (int s = 0; s<loader.getSchemas().size(); s++){
                Metrics metrics = new Metrics(loader.getSchemas().get(s));
                System.out.print(loader.getSchemas().get(s).getName());
                switch (coverageOption) {
                    case path:
                        System.out.print("\t" + metrics.queryPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryPath().getSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case subpath:
                        System.out.print("\t" + metrics.querySubPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.querySubPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.querySubPath().getSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case indpath:
                        System.out.print("\t" + metrics.queryIndirectPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryIndirectPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryIndirectPath().getSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case inv_path:
                        System.out.print("\t" + metrics.queryPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryPath().getInvSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case inv_subpath:
                        System.out.print("\t" + metrics.querySubPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.querySubPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.querySubPath().getInvSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case inv_indpath:
                        System.out.print("\t" + metrics.queryIndirectPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryIndirectPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryIndirectPath().getInvSchemaCoverage(loader.getQueries().get(i)));
                        break;
                    case direct_edge:
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryEdge().getSchemaDirectEdgeCoverage(loader.getQueries().get(i)));
                        break;
                    case inverted_edge:
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaInvertedEdgeCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaInvertedEdgeCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryEdge().getSchemaInvertedEdgeCoverage(loader.getQueries().get(i)));
                        break;
                    case alltypes_edge:
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaAllEdgeCoverage(loader.getQueries().get(i)).getValue());
                        System.out.print("\t" + metrics.queryEdge().getMaxSchemaAllEdgeCoverage(loader.getQueries().get(i)).getDepth());
                        System.out.println("\t" + metrics.queryEdge().getSchemaAllEdgeCoverage(loader.getQueries().get(i)));
                        break;
                }
            } 
            System.out.println("");
        }        
    } 
     
}
