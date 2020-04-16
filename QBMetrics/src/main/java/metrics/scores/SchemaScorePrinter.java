/*
 * Está classe tem por objetivo calcular o score dos esquemas.
 *
 * Observações:
 *  - Usa pesos para calcular as métricas path, subpath e indpath.
 *  - Tem métodos para calcular o score do esquema por: path, inverted path e edges.
 *  - Tem UM método para calcular e listar os scores do esquema considerando todas as 9 métricas.
 *
 */
package metrics.scores;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.nosql_schema.NoSQLSchema;
import metrics.Metrics;
import metrics.Printer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import metrics.test_loader.AbstractTestInputLoader;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class SchemaScorePrinter {
    private AbstractTestInputLoader loader;
    public double path_weight = 1.0;
    public double subpath_weight = 0.7;
    public double indpath_weight = 0.5;
    public boolean calcWithDepth = true;
    public ArrayList<Double> queries_weights = new ArrayList<>();

    public SchemaScorePrinter(AbstractTestInputLoader loader) {
        this.loader = loader;
        
        // Definição padrão: Peso das consultas.
        //  Caso o usuário não modifique o peso, todas as consultas terão o mesmo peso.
        //   Ou seja, equivalente a média aritmética da consultas.
        double weight = new BigDecimal( 1.0 / this.loader.getQueries().size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        for (int i=0; i<this.loader.getQueries().size(); i++){
            queries_weights.add(weight);
        }
        
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printPathScore() {
        System.out.println("*********************************************************************************");
        System.out.println("SCHEMA PATH SCORE");
        System.out.println(" Input Parameters:");
        System.out.println(" - Queries Weights: " + this.queries_weights);
        System.out.println(" - Path Weights: path=" + this.path_weight + ", subpath=" + this.subpath_weight + ", indpath=" + this.indpath_weight);
        System.out.println(" - Path Depth: " + this.calcWithDepth);
        System.out.println(" Formulas:");
        System.out.println("  QScore  = MAX(inv_path*w, inv_subpath*w, inv_indirectpath*w)");
        System.out.println("  SScore  = SUM(Qscore_1 * QWeight_1,...,Qscore_n * QWeight_n)");
        System.out.println("  Obs.: Não tenho função para identificar JOIN ainda!");
        System.out.println("*********************************************************************************");
        
        for (int s = 0; s<loader.getSchemas().size(); s++){
            Metrics metrics = new Metrics(loader.getSchemas().get(s));
            System.out.println("Schema: "+ loader.getSchemas().get(s).getName());
            System.out.println("Coverage\t\t\t\t\t\t\t\tMetrics Value (path_weight, depth, query_weight)");
            System.out.println("Query\tPath\tD\tSubpath\tD\tIndpath\tD\t|\tQuery\tPath\tSubpath\tIndpath\t|\tQScore\tQScore(W)");
            
            double schemaScore = 0.0;
            for (int i=0; i<loader.getQueries().size(); i++){  
                // Mostrando a cobertura do esquema.
                String line = "Q"+(i+1);
                line += "\t"+metrics.queryPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue() +"\t"+metrics.queryPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth();
                line += "\t"+metrics.querySubPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue() +"\t"+metrics.querySubPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth();
                line += "\t"+metrics.queryIndirectPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getValue() +"\t"+metrics.queryIndirectPath().getMaxSchemaCoverage(loader.getQueries().get(i)).getDepth();
                line += "\t|";
                
                // Calculando o valor da métrica, considerando pesos e profundidade dos caminhos.
                double path    = calcPathMetricValue(metrics, loader.getQueries().get(i));
                double subpath = calcSubPathMetricValue(metrics, loader.getQueries().get(i));
                double indpath = calcIndPathMetricValue(metrics, loader.getQueries().get(i));
                double maxvalue = Math.max(path, Math.max(subpath, indpath));
                // Calculando o valor do QScore, considerando o peso da consulta.
                double qscore_with_weight = maxvalue * queries_weights.get(i);
                
                line += "\tQ"+(i+1);
                line += "\t"+path;
                line += "\t"+subpath;
                line += "\t"+indpath;
                line += "\t|";
                line += "\t"+maxvalue;
                line += "\t"+qscore_with_weight;
                System.out.println(line);
            }
            String score = "SScore: ";
            if (calcWithDepth) score = "SScore (depth): ";
            System.out.println(score + this.getPathScore(loader.getSchemas().get(s), loader.getQueries()));
            System.out.println("");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printInvPathScore() {
        System.out.println("*********************************************************************************");
        System.out.println("SCHEMA *INVERTED PATH SCORE");
        System.out.println(" Input Parameters:");
        System.out.println(" - Queries Weights: " + this.queries_weights);
        System.out.println(" - Path Weights: path=" + this.path_weight + ", subpath=" + this.subpath_weight + ", indpath=" + this.indpath_weight);
        System.out.println(" - Path Depth: " + this.calcWithDepth);
        System.out.println(" Formulas:");
        System.out.println("  QScore  = MAX(inv_path*w, inv_subpath*w, inv_indirectpath*w)");
        System.out.println("  SScore  = SUM(Qscore_1 * QWeight_1,...,Qscore_n * QWeight_n)");
        System.out.println("  Obs.: Não tenho função para identificar JOIN ainda!");
        System.out.println("*********************************************************************************");
        
        for (int s = 0; s<loader.getSchemas().size(); s++){
            Metrics metrics = new Metrics(loader.getSchemas().get(s));
            System.out.println("Schema: "+ loader.getSchemas().get(s).getName());
            System.out.println("Coverage\t\t\t\t\t\t\t\tMetrics Value (path_weight, depth, query_weight)");
            System.out.println("Query\tPath\tD\tSubpath\tD\tIndpath\tD\t|\tQuery\tPath\tSubpath\tIndpath\t|\t*QScore\t*QScore(W)");
            
            double schemaScore = 0.0;
            for (int i=0; i<loader.getQueries().size(); i++){  
                // Mostrando a cobertura do esquema.
                String line = "Q"+(i+1);
                line += "\t"+metrics.queryPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue() +"\t"+metrics.queryPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth();
                line += "\t"+metrics.querySubPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue() +"\t"+metrics.querySubPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth();
                line += "\t"+metrics.queryIndirectPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getValue() +"\t"+metrics.queryIndirectPath().getInvMaxSchemaCoverage(loader.getQueries().get(i)).getDepth();
                line += "\t|";
                
                // Calculando o valor da métrica, considerando pesos e profundidade dos caminhos.
                double path    = calcInvPathMetricValue(metrics, loader.getQueries().get(i));
                double subpath = calcInvSubPathMetricValue(metrics, loader.getQueries().get(i));
                double indpath = calcInvIndPathMetricValue(metrics, loader.getQueries().get(i));
                double maxvalue = Math.max(path, Math.max(subpath, indpath));
                // Calculando o valor do QScore, considerando o peso da consulta.
                double qscore_with_weight = maxvalue * queries_weights.get(i);
                
                line += "\tQ"+(i+1);
                line += "\t"+path;
                line += "\t"+subpath;
                line += "\t"+indpath;
                line += "\t|";
                line += "\t"+maxvalue;
                line += "\t "+qscore_with_weight;
                System.out.println(line);
            }
            String score = "SScore: ";
            if (calcWithDepth) score = "SScore (depth): ";
            System.out.println(score + this.getInvPathScore(loader.getSchemas().get(s), loader.getQueries()));
            System.out.println("");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printJoinScore() {
        System.out.println("*********************************************************************************");
        System.out.println("JOIN SCORE");
        System.out.println("   NÃO ESTOU CONSIDERANDO PESO DAS CONSULTAS NESSA MÉTRICA, DEVO CONSIDERAR?");
        System.out.println("  Retorna o número máximo de coleções necessárias para responder cada consulta.");
        System.out.println("  Score: valor normalizado entre 0..1, sendo 1 o valor ideal.");
        System.out.println("  Score = 1.0, significa que cada consulta do conjunto pode ser respondida por uma coleção.");
        System.out.println("  Score < 1.0, quanto menor o score, mais coleções são necessárias para responder o conjunto de consultas.");
        System.out.println("*********************************************************************************");
        
        for (int s = 0; s<loader.getSchemas().size(); s++){
            Metrics metrics = new Metrics(loader.getSchemas().get(s));
            System.out.println("Schema: "+ loader.getSchemas().get(s).getName());
            System.out.println("Query\tReqCols");
            
            int total_number_of_collections = 0;
            for (int i=0; i<loader.getQueries().size(); i++){  
                System.out.print((i+1));
                System.out.println("\t"+metrics.queryJoin().getMaxSchemaCoverage(loader.getQueries().get(i), false).getNumberOfCollection());
                
                total_number_of_collections += metrics.queryJoin().getMaxSchemaCoverage(loader.getQueries().get(i), false).getNumberOfCollection();
            }
            
            double score = total_number_of_collections - loader.getQueries().size();
            score = new BigDecimal( (total_number_of_collections - score) / total_number_of_collections ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            
            System.out.print("Score");
            System.out.print("\t"+score);
            System.out.println("\n");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Essa versão tem os campos QScore e formatação para imprimir os dados alinhados.
    public void printEdgeScore() {
        System.out.println("*********************************************************************************");
        System.out.println("SCHEMA EDGE SCORE");
        System.out.println(" Input Parameters:");
        System.out.println(" - Queries Weights: " + this.queries_weights);
        System.out.println(" - Path Weights: path=" + this.path_weight + ", subpath=" + this.subpath_weight + ", indpath=" + this.indpath_weight);
        System.out.println(" - Path Depth: " + this.calcWithDepth);
        System.out.println("  Mostra Cobertura e QScore, onde QScore usa o peso da consulta no calculo.");
        System.out.println("  Score  = SUM(Q1_EdgeCov * Weight1, Q2_EdgeCov * Weight2, ..., Qn_EdgeCov_n)");
        System.out.println("*********************************************************************************");
        
        for (int s = 0; s<loader.getSchemas().size(); s++){
            Metrics metrics = new Metrics(loader.getSchemas().get(s));
            System.out.println("Schema: "+ loader.getSchemas().get(s).getName());
            System.out.println("Query\tDirEdgeCov\tQScore\tInvEdgeCov\tQScore\tAllEdgeCov\tQScore");
            
            for (int i=0; i<loader.getQueries().size(); i++){  
                System.out.print((i+1));
                
                double dirEdgeValue = metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(loader.getQueries().get(i)).getValue();
                double dirEdgeQScore = new BigDecimal( dirEdgeValue * queries_weights.get(i) ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                System.out.print("\t"+Printer.insertSpaceChars(String.valueOf(dirEdgeValue), 11));
                System.out.print("\t"+dirEdgeQScore);
                
                double invEdgeValue = metrics.queryEdge().getMaxSchemaInvertedEdgeCoverage(loader.getQueries().get(i)).getValue();
                double invEdgeQScore = new BigDecimal( invEdgeValue * queries_weights.get(i) ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                System.out.print("\t"+Printer.insertSpaceChars(String.valueOf(invEdgeValue),11));
                System.out.print("\t"+invEdgeQScore);
                
                double allEdgeValue = metrics.queryEdge().getMaxSchemaAllEdgeCoverage(loader.getQueries().get(i)).getValue();
                double allEdgeQScore = new BigDecimal( allEdgeValue * queries_weights.get(i) ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                System.out.print("\t"+Printer.insertSpaceChars(String.valueOf(allEdgeValue),11));
                System.out.println("\t"+allEdgeQScore);
            }
            System.out.print("Score");
            System.out.print("\t"+Printer.insertSpaceChars("",11)+"\t"+this.getDirectEdgeScore(loader.getSchemas().get(s), loader.getQueries()));
            System.out.print("\t"+Printer.insertSpaceChars("",11)+"\t"+this.getInvertedEdgeScore(loader.getSchemas().get(s), loader.getQueries()));
            System.out.print("\t"+Printer.insertSpaceChars("",11)+"\t"+this.getAllEdgeScore(loader.getSchemas().get(s), loader.getQueries()));
            System.out.println("\n");
        }
    }
    
//    public void printEdgeScore() {
//        System.out.println("*********************************************************************************");
//        System.out.println("SCHEMA EDGE SCORE");
//        System.out.println("  Score  = SUM(Q1_EdgeCov * Weight1, Q2_EdgeCov * Weight2, ..., Qn_EdgeCov_n)");
//        System.out.println("*********************************************************************************");
//        
//        for (int s = 0; s<loader.getSchemas().size(); s++){
//            Metrics metrics = new Metrics(loader.getSchemas().get(s));
//            System.out.println("Schema: "+ loader.getSchemas().get(s).getName());
//            System.out.println("Query\tDirectEdgeCov\tInvertedEdgeCov\tAllEdgeCov");
//            
//            for (int i=0; i<loader.getQueries().size(); i++){  
//                System.out.print((i+1));
//                System.out.print("\t"+metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(loader.getQueries().get(i)).getValue());
//                System.out.print("\t        "+metrics.queryEdge().getMaxSchemaInvertedEdgeCoverage(loader.getQueries().get(i)).getValue());
//                System.out.println("\t        "+metrics.queryEdge().getMaxSchemaAllEdgeCoverage(loader.getQueries().get(i)).getValue());
//            }
//            System.out.print("Score");
//            System.out.print("\t"+this.getDirectEdgeScore(loader.getSchemas().get(s), loader.getQueries()));
//            System.out.print("\t        "+this.getInvertedEdgeScore(loader.getSchemas().get(s), loader.getQueries()));
//            System.out.print("\t        "+this.getAllEdgeScore(loader.getSchemas().get(s), loader.getQueries()));
//            System.out.println("\n");
//        }
//    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SCHEMAS SCORE
    //  - MOSTRA UM SUMÁRIO DE TODAS AS MÉTRICAS POR ESQUEMA
    //  - OS DADOS SÃO NORMALIZADOS, ONDE CADA CONSULTA TEM PESO IGUAL.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void printAllMetricsScore() {
        System.out.println("*********************************************************************************");
        System.out.println("FINAL SCHEMAs SCORE");
        System.out.println(" Input Parameters:");
        System.out.println(" - Queries Weights: " + this.queries_weights);
        System.out.println(" - Path Weights: path=" + this.path_weight + ", subpath=" + this.subpath_weight + ", indpath=" + this.indpath_weight);
        System.out.println(" - Path Depth: " + this.calcWithDepth);
        System.out.println(" Output:");
        System.out.println(" - Sum(QScore1 * QWeight1, QScore2 * QWeight2, ...)");
        System.out.println("*********************************************************************************");
        
        System.out.println("Schema\tDirEdges\tInvEdges\tAllEdges\tPath\t   *Path\tJoin");
        for (int s = 0; s<loader.getSchemas().size(); s++){
            NoSQLSchema schema = loader.getSchemas().get(s);
            Metrics metrics = new Metrics(schema);

            System.out.print(schema.getName());
            System.out.print("\t"+this.getDirectEdgeScore(schema, loader.getQueries()));
            System.out.print("\t        "+this.getInvertedEdgeScore(schema, loader.getQueries()));
            System.out.print("\t        "+this.getAllEdgeScore(schema, loader.getQueries()));
            System.out.print("\t        "+this.getPathScore(schema, loader.getQueries()));
            System.out.print("\t    "+this.getInvPathScore(schema, loader.getQueries()));
            System.out.println("\t        "+this.getJoinScore(schema, loader.getQueries()));
        }
        System.out.println("");
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Métodos para calcular o valor da métrica para TODO o esquema;
    //  - Considera pesos para cada consulta.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    public double getJoinScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries) {
        Metrics metrics = new Metrics(schema);
            
        int reqCollections = 0;
        for (int i=0; i<queries.size(); i++){  
            reqCollections += metrics.queryJoin().getMaxSchemaCoverage(loader.getQueries().get(i), false).getNumberOfCollection();
        }
        // Considerando que o ideal é ter reqCollections == queries.size(), ou seja, cada consulta é respondida por uma consulta.
        //  numberOfAdicionalCollections denota o número de coleções extras para responder ao conjunto de consultas queries.
        double numberOfAdicionalCollections = reqCollections - queries.size();
        
        // Tem um termo para esse calculo, agora não me lembro... ver artigos do desktop.
        double score = new BigDecimal( (reqCollections - numberOfAdicionalCollections) / reqCollections ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        
        return score;
    }
    
    public double getDirectEdgeScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        double score = 0.0;
        for (int i=0; i<loader.getQueries().size(); i++){  
            score += metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(queries.get(i)).getValue() * queries_weights.get(i);
        }
//        score = new BigDecimal( score / queries.size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();   
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        return score;
    }
    
    public double getInvertedEdgeScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        double score = 0.0;
        for (int i=0; i<loader.getQueries().size(); i++){  
            score += metrics.queryEdge().getMaxSchemaInvertedEdgeCoverage(queries.get(i)).getValue() * queries_weights.get(i);
        }
//        score = new BigDecimal( score / queries.size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();  
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        return score;
    }
    
    public double getAllEdgeScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        double score = 0.0;
        for (int i=0; i<loader.getQueries().size(); i++){  
            score += metrics.queryEdge().getMaxSchemaAllEdgeCoverage(queries.get(i)).getValue() * queries_weights.get(i);
        }
//        score = new BigDecimal( score / queries.size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    public double getPathScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        double score = 0.0;
        for (int i=0; i<loader.getQueries().size(); i++){  
            double path    = calcPathMetricValue(metrics, queries.get(i));
            double subpath = calcSubPathMetricValue(metrics, queries.get(i));
            double indpath = calcIndPathMetricValue(metrics, queries.get(i));
            double maxvalue = Math.max(path, Math.max(subpath, indpath)) * queries_weights.get(i);
            score += maxvalue;
        }
        //score = new BigDecimal( score / queries.size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    public double getInvPathScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        double score = 0.0;
        for (int i=0; i<loader.getQueries().size(); i++){  
            double path    = calcInvPathMetricValue(metrics, queries.get(i));
            double subpath = calcInvSubPathMetricValue(metrics, queries.get(i));
            double indpath = calcInvIndPathMetricValue(metrics, queries.get(i));            
            double maxvalue = Math.max(path, Math.max(subpath, indpath)) * queries_weights.get(i);
            score += maxvalue;
        }
        //score = new BigDecimal( score / queries.size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Métodos para calcular o valor de cada métrica (PATH, SUBPATH, INDPATH)
    //  - Usa a cobertura máxima da consulta e
    //  - Considera pesos dos caminhos e profundidade no cálculo.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public double calcInvPathMetricValue(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double path = path_weight * metrics.queryPath().getInvMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryPath().getInvMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            path /= depth;
        }
        return new BigDecimal( path ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double calcInvSubPathMetricValue(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double subpath = subpath_weight * metrics.querySubPath().getInvMaxSchemaCoverage(query).getValue();
        int depth = metrics.querySubPath().getInvMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            subpath /= depth;
        }
        return new BigDecimal( subpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double calcInvIndPathMetricValue(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double indpath = indpath_weight * metrics.queryIndirectPath().getInvMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryIndirectPath().getInvMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            indpath /= depth;
        }
        return new BigDecimal( indpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double calcPathMetricValue(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double path = path_weight * metrics.queryPath().getMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryPath().getMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            path /= depth;
        }
        return new BigDecimal( path ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double calcSubPathMetricValue(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double subpath = subpath_weight * metrics.querySubPath().getMaxSchemaCoverage(query).getValue();
        int depth = metrics.querySubPath().getMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            subpath /= depth;
        }
        return new BigDecimal( subpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double calcIndPathMetricValue(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double indpath = indpath_weight * metrics.queryIndirectPath().getMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryIndirectPath().getMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            indpath /= depth;
        }
        return new BigDecimal( indpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
}