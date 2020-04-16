/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import dag.nosql_schema.NoSQLSchema;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class Printer {
    
    
    public static String printStructuralMetrics(NoSQLSchema schema){
        // Objeto que encapsula as métricas sobre NoSQLSchema...
        Metrics metrics = new Metrics(schema);
        
        String out = "";
        out += "Schema Name: " + schema.getName();
        out += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------";
        out += "\n";
        out += "\nMetric Summary:";
        out += "\n  Complexity*:  " + metrics.size().getSchemaSize(false);
        out += "\n  Complexity**: " + metrics.size().getSchemaSize(true);
        out += "\n  Redundacy Rate Added (%): " + metrics.size().getRedundancyRate();
        out += "\n  Redundacy Factor: " + metrics.size().getRedundancyFactor();
        out += "\n  Max Depth: " + metrics.depth().getDepthOfSchema();
        out += "\n  Obs.:";
        out += "\n    Complexity* = atomic_attr + doc_attr + array_doc_attr + array_atomic_attr";
        out += "\n    Complexity** = atomic_attr + doc_attr*2 + array_doc_attr*3 + array_atomic_attr";
        out += "\n";
        out += "\nNumber of:";
        out += "\n  collections: " + metrics.size().getNumberOfCollectionsInSchema();
        out += "\n  documents: " + metrics.size().getNumberOfDocumentsInSchema();
        out += "\n  arrays of documents: " + metrics.size().getNumberOfArraysOfDocumentsInSchema();
        out += "\n  atomic attributes: " + metrics.size().getNumberOfAtomicAttributesInSchema();
        out += "\n";
        out += "\nList of:";
        out += "\n  collections: " + metrics.getListOfCollectionsInSchema();
        out += "\n  documents: " + metrics.getListOfDocumentsInSchema();
        out += "\n  arrays of documents: " + metrics.getListOfArraysOfDocumentInSchema();
        out += "\n";
        out += "\nSize of each collection:";
        for (String collection : metrics.getListOfCollectionsInSchema()){
            String msg = "\n  " + collection + ": "+metrics.size().getCollectionSize(collection, false);
            msg += "   (atomic attributes: " + metrics.size().getNumberOfAtomicAttributes(collection);
            msg += "   documents: " + metrics.size().getNumberOfDocuments(collection);
            msg += "   arrays of documents: " + metrics.size().getNumberOfArraysOfDocuments(collection);
            out +=  msg + ")";
        }
        out += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------";
        return out;
    }
    
    public static String printStructuralMetrics(NoSQLSchema schema, String collectionName){
        // Objeto que encapsula as métricas sobre NoSQLSchema...
        Metrics metrics = new Metrics(schema);
        
        String out =  "";
        out += "Schema Name: " + schema.getName();   
        out += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------";
//        out += "\nSchema Name: " + schema.getName();        
        out += "\nCollection: " + collectionName;   
        out += "\n";
        out += "\nMetric Summary: "; 
        out += "\n  Complexity*: " + metrics.size().getCollectionSize(collectionName, false);
        out += "\n  Complexity**: " + metrics.size().getCollectionSize(collectionName, true);
        out += "\n  Redundacy Rate Added (%): ??";// + metrics.size().getRedundancyRate();
        out += "\n  Redundacy Factor: ?"; // + metrics.size().getRedundancyFactor();
        out += "\n  Max Depth: " + metrics.depth().getDepthOfCollection(collectionName);
        out += "\n  Obs.:";
        out += "\n    Complexity* = atomic_attr + doc_attr + array_doc_attr + array_atomic_attr";
        out += "\n    Complexity** = atomic_attr + doc_attr*2 + array_doc_attr*3 + array_atomic_attr";
        out += "\n";
        out += "\nNumber of:";
        out += "\n  documents: " + metrics.size().getNumberOfDocuments(collectionName);
        out += "\n  arrays of documents: " + metrics.size().getNumberOfArraysOfDocuments(collectionName);
        out += "\n  atomic attributes: " + metrics.size().getNumberOfAtomicAttributes(collectionName);
        out += "\n";
        out += "\nList of:";
        out += "\n  documents: " + metrics.getListOfDocuments(collectionName);
        out += "\n  arrays of documents: " + metrics.getListOfArraysOfDocuments(collectionName);
        out += "\n";
        out += "\nCollection size: " + +metrics.size().getCollectionSize(collectionName, false);        
        out += "\n  atomic attributes: " + metrics.size().getNumberOfAtomicAttributes(collectionName);
        out += "\n  documents: " + metrics.size().getNumberOfDocuments(collectionName);
        out += "\n  arrays of documents: " + metrics.size().getNumberOfArraysOfDocuments(collectionName);
        out += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------";
        return out;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 
    // PRINTER QUERY METRIC METHODS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static void printQueries(ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        Metrics metrics = new Metrics(null);
        System.out.println("Queries:");        
        for (int i=0; i<queries.size(); i++){
            String out = "";
            out += i+1+": "+metrics.path().getPaths(queries.get(i));            
            System.out.println(out);
        }
    }
    
    public static void printQueryMetrics_query_x_metric_groupBy_schema(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        Metrics metrics = new Metrics(schema);            
        int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
                
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("SCHEMA NAME: " + schema.getName());
        //System.out.println("Schema Paths: " + metrics.path().getSchemaPaths());
        
        ////////////////////////////////////////////////////
        // 1 COVERAGE IN SCHEMA
        //System.out.println("Coverage In Schema (* = Invert): ");        
        String header = "Queries\t\t";
        header += "Vertex  \t"; 
        header += "Path    \t"; 
        header += "SubPath \t"; 
        header += "IndPath \t";
        header += "*Path   \t";
        header += "*SubPath\t";
        header += "*IndPath\t";
        header += "QScore  \t";
        header += "*QScore \t";
        System.out.println(header);        
        
        for (int i=0; i<queries.size(); i++){
            String out = i+1+"\t\t";
            out += metrics.query().getVertexCoverageInSchema(queries.get(i))+"      \t";    
            out += metrics.query().getPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getSubPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getIndirectPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getInvertPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getInvertSubPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getInvertIndirectPathCoverageInSchema(queries.get(i))+"      \t";            
            out += getQueryScore(metrics, queries.get(i))+metrics.query().getVertexCoverageInSchema(queries.get(i))+"      \t";
            out += getQueryStarScore(metrics, queries.get(i))+metrics.query().getVertexCoverageInSchema(queries.get(i));
            System.out.println(out);
        } 
        
        // Calculando valor médio de cada métrica para todo o conjunto de consultas.
        double[] meanValues = new double[7]; // número de métricas = 6.
        for (int i=0; i<queries.size(); i++){            
            meanValues[0] += metrics.query().getVertexCoverageInSchema(queries.get(i));
            meanValues[1] += metrics.query().getPathCoverageInSchema(queries.get(i));
            meanValues[2] += metrics.query().getSubPathCoverageInSchema(queries.get(i));
            meanValues[3] += metrics.query().getIndirectPathCoverageInSchema(queries.get(i));
            meanValues[4] += metrics.query().getInvertPathCoverageInSchema(queries.get(i));
            meanValues[5] += metrics.query().getInvertSubPathCoverageInSchema(queries.get(i));
            meanValues[6] += metrics.query().getInvertIndirectPathCoverageInSchema(queries.get(i));
        }        
        // Calculando média
        for (int i=0; i<6; i++) meanValues[i] = new BigDecimal( meanValues[i] / queries.size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        System.out.println(
                "Avg\t\t"+meanValues[0]
                +"      \t"+meanValues[1]
                +"      \t"+meanValues[2]
                +"      \t"+meanValues[3]
                +"      \t"+meanValues[4]
                +"      \t"+meanValues[5]
                +"      \t"+meanValues[6]);
        
        // Imprimindo Score Final do Esquema (Somatório do valor médio de cada métrica).
        double score = 0;
        double star_score = 0;
        double final_score = 0;
        
        score = meanValues[1] + meanValues[2] + meanValues[3];
        star_score = meanValues[4] + meanValues[5] + meanValues[6];
        final_score = meanValues[0] + score + star_score;
        
        System.out.println("Score      \t\t"+ new BigDecimal(score).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        System.out.println("Score*     \t\t"+ new BigDecimal(star_score).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        System.out.println("Final Score\t\t"+ new BigDecimal(final_score).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        
//        for (int i=0; i<meanValues.length; i++) score += meanValues[i];
//        System.out.println("Score\t\t"+ new BigDecimal(score).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
    }
    
    public static void printQueryMetrics_query_x_metric_groupBy_collection_schema(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        Metrics metrics = new Metrics(schema);            
        int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
        
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("SCHEMA NAME: " + schema.getName());
        System.out.println("Schema Paths: " + metrics.path().getSchemaPaths());
        
        ////////////////////////////////////////////////////
        // 2 COVERAGE BY COLLECTION        
        // Para cada coleção do schema, extrair os resultados para cada query metric.
        for (String colName : metrics.getListOfCollectionsInSchema()){
            System.out.println("Coverage By Collection: " + colName);        
            String header = "Queries\t\t";
            header += "Vertex\t"; 
            header += "Edge\t"; 
            header += "Path\t"; 
            header += "SubPath\t"; 
            header += "IndPath\t";
            header += "InvPath";
            System.out.println(header);        

            for (int i=0; i<queries.size(); i++){
                String out = i+1+"\t\t";
                out += metrics.query().getVertexCoverage(colName, queries.get(i))+"\t";    
                out += metrics.query().getEdgeCoverage(colName, queries.get(i))+"\t";
                out += metrics.query().getPathCoverage(colName, queries.get(i))+"\t";
                out += metrics.query().getSubPathCoverage(colName, queries.get(i))+"\t";
                out += metrics.query().getIndirectPathCoverage(colName, queries.get(i))+"\t";
                out += metrics.query().getInvertPathCoverage(colName, queries.get(i))+"\t";
                System.out.println(out);
            }        
        }
    }
    
    public static void printQueryMetrics_query_x_collection_groupBy_metric_schema(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        Metrics metrics = new Metrics(schema);            
        int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
        
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("SCHEMA NAME: " + schema.getName());
        System.out.println("Schema Paths: " + metrics.path().getSchemaPaths());
        
        String metricNames[] = {"Vertex", "Edge", "Path", "SubPath", "IndPath", "InvPath"};
        
        ////////////////////////////////////////////////////
        // 3 COVERAGE BY METRIC  
        // Para cada query metric, imprimir os resultados para cada coleção.
        for (String metricName : metricNames){
            System.out.println("Coverage By Metric: " + metricName);
            // Imprime o cabeçalho dos resultados.
            String header = "Queries\t\t";
            for (String colName : metrics.getListOfCollectionsInSchema()){
                header += colName + "\t"; 
            }
            System.out.println(header); 
            // Resultados            
            for (int i=0; i<queries.size(); i++){
                String out = i+1+"\t\t";
                for (String colName : metrics.getListOfCollectionsInSchema()){
                    String value = "";
                    switch (metricName){
                        case "Vertex":
                            value = String.valueOf( metrics.query().getVertexCoverage(colName, queries.get(i)) );
                            out += insertSpaceChars(value, colName.length())+"\t";
                            break;
                        case "Edge":
                            value = String.valueOf( metrics.query().getEdgeCoverage(colName, queries.get(i)) );
                            out += insertSpaceChars(value, colName.length())+"\t";                            
                            break;
                        case "Path":
                            value = String.valueOf( metrics.query().getPathCoverage(colName, queries.get(i)) );
                            out += insertSpaceChars(value, colName.length())+"\t";                            
                            break;
                        case "SubPath":
                            value = String.valueOf( metrics.query().getSubPathCoverage(colName, queries.get(i)) );
                            out += insertSpaceChars(value, colName.length())+"\t";
                            break;
                        case "IndPath":
                            value = String.valueOf( metrics.query().getIndirectPathCoverage(colName, queries.get(i)) );
                            out += insertSpaceChars(value, colName.length())+"\t";
                            break;
                        case "InvPath":
                            value = String.valueOf( metrics.query().getInvertPathCoverage(colName, queries.get(i)) );
                            out += insertSpaceChars(value, colName.length())+"\t";
                            break;
                    }
                }
                System.out.println(out);
            }
        }
    }
    
    
    public static void printQueryMetrics_schemas_x_metrics_groupby_query(ArrayList<NoSQLSchema> schemas, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("SCHEMA NAME: All Schemas");
        System.out.println("Coverage: Schemas X Metrics Group By Query");
        
        ////////////////////////////////////////////////////
        // 4 COVERAGE BY QUERY        
        // Para cada consulta, extrair métricas x esquema...
        int queryCount = 1;
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> query : queries){
            System.out.println("Query: " + queryCount++ + " " + new Metrics(null).path().getPaths(query)); 
            
            String header = "Schema\t\t";
            header += "Vertex\t"; 
            header += "Edge\t"; 
            header += "Path\t"; 
            header += "SubPath\t"; 
            header += "IndPath\t";
            header += "InvPath\t";
            header += "Score";
            System.out.println(header); 
            
            // Para cada esquema, extrair os valores da métricas...
            for (NoSQLSchema schema : schemas){
                Metrics metrics = new Metrics(schema);
                double score = 0;
                String out = schema.getName()+"\t\t";
                out += metrics.query().getVertexCoverageInSchema(query)+"\t";    
                out += metrics.query().getEdgeCoverageInSchema(query)+"\t";
                out += metrics.query().getPathCoverageInSchema(query)+"\t";
                out += metrics.query().getSubPathCoverageInSchema(query)+"\t";
                out += metrics.query().getIndirectPathCoverageInSchema(query)+"\t";
                out += metrics.query().getInvertPathCoverageInSchema(query)+"\t";
                
                // Calculando score da Query.
                score += metrics.query().getVertexCoverageInSchema(query);
                score += metrics.query().getEdgeCoverageInSchema(query);
                score += metrics.query().getPathCoverageInSchema(query);
                score += metrics.query().getSubPathCoverageInSchema(query);
                score += metrics.query().getIndirectPathCoverageInSchema(query);
                score += metrics.query().getInvertPathCoverageInSchema(query);
                
                System.out.println(out+score);                
            }
        }        
    }

    
    public static void printQueryMetrics_schema_x_scoreQuery(ArrayList<NoSQLSchema> schemas, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("SCHEMA NAME: All Schemas");
        System.out.println("Coverage: Schemas X Score Query (vertex+edge+path+subpath+indpath+invpath)");
        
        String header = "Schema\t\t";
        for (int i=1; i<queries.size(); i++){
            header += "Q"+i+"\t";
        }
        header += "Q"+queries.size();
        System.out.println(header); 
        
        for (NoSQLSchema schema : schemas){
            Metrics metrics = new Metrics(schema);
            
            String out = schema.getName()+"\t\t";            
            for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> query : queries){
                // Calculando score da Query.
                double score = 0;
                score += metrics.query().getVertexCoverageInSchema(query);
                score += metrics.query().getEdgeCoverageInSchema(query);
                score += metrics.query().getPathCoverageInSchema(query);
                score += metrics.query().getSubPathCoverageInSchema(query);
                score += metrics.query().getIndirectPathCoverageInSchema(query);
                score += metrics.query().getInvertPathCoverageInSchema(query);
                out += score + "\t";
            }
            System.out.println(out);
        }
    }
    
    public static double getQueryMetricsSum(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double sum = 0;
        sum += metrics.query().getVertexCoverageInSchema(query);    
        //sum += metrics.query().getEdgeCoverageInSchema(query);
        sum += metrics.query().getPathCoverageInSchema(query);
        sum += metrics.query().getSubPathCoverageInSchema(query);
        sum += metrics.query().getIndirectPathCoverageInSchema(query);
        sum += metrics.query().getInvertPathCoverageInSchema(query);
        sum += metrics.query().getInvertSubPathCoverageInSchema(query);
        sum += metrics.query().getInvertIndirectPathCoverageInSchema(query);
        return sum;
    }
    
    // By Schema
    public static double getQueryScore(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double sum = 0;        
        sum += metrics.query().getPathCoverageInSchema(query);
        sum += metrics.query().getSubPathCoverageInSchema(query);
        sum += metrics.query().getIndirectPathCoverageInSchema(query);
        // Contabilizando a métrica Vertex também.
        sum += metrics.query().getVertexCoverageInSchema(query);
        return sum;
    }
    
    // By Schema
    public static double getQueryStarScore(Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double sum = 0;
        sum += metrics.query().getInvertPathCoverageInSchema(query);
        sum += metrics.query().getInvertSubPathCoverageInSchema(query);
        sum += metrics.query().getInvertIndirectPathCoverageInSchema(query);
        // Contabilizando a métrica Vertex também.
        sum += metrics.query().getVertexCoverageInSchema(query);
        return sum;
    }
    
    // By Collection
    public static double getQueryScore(String collectionName, Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double sum = 0;        
        sum += metrics.query().getPathCoverage(collectionName, query);
        sum += metrics.query().getSubPathCoverage(collectionName, query);
        sum += metrics.query().getIndirectPathCoverage(collectionName, query);
        // Contabilizando a métrica Vertex também.
        sum += metrics.query().getVertexCoverage(collectionName, query);
        return sum;
    }
    
    // By Collection
    public static double getQueryStarScore(String collectionName, Metrics metrics, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double sum = 0;
        sum += metrics.query().getInvertPathCoverage(collectionName, query);
        sum += metrics.query().getInvertSubPathCoverage(collectionName, query);
        sum += metrics.query().getInvertIndirectPathCoverage(collectionName, query);
        // Contabilizando a métrica Vertex também.
        sum += metrics.query().getVertexCoverage(collectionName, query);
        return sum;
    }
    
    public static double getSchemaAvgByMetric(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, String metricName){
        Metrics metrics = new Metrics(schema);
        
        double meanValue = 0;
        for (int i=0; i<queries.size(); i++){
            if (metricName.equalsIgnoreCase("vertex"))      meanValue += metrics.query().getVertexCoverageInSchema(queries.get(i));
            if (metricName.equalsIgnoreCase("path"))        meanValue += metrics.query().getPathCoverageInSchema(queries.get(i));
            if (metricName.equalsIgnoreCase("subpath"))     meanValue += metrics.query().getSubPathCoverageInSchema(queries.get(i));
            if (metricName.equalsIgnoreCase("indpath"))     meanValue += metrics.query().getIndirectPathCoverageInSchema(queries.get(i));
            if (metricName.equalsIgnoreCase("*path"))       meanValue += metrics.query().getInvertPathCoverageInSchema(queries.get(i));
            if (metricName.equalsIgnoreCase("*subpath"))    meanValue += metrics.query().getInvertSubPathCoverageInSchema(queries.get(i));
            if (metricName.equalsIgnoreCase("*indpath"))    meanValue += metrics.query().getInvertIndirectPathCoverageInSchema(queries.get(i));
        }
        
        meanValue = meanValue / queries.size();
        return new BigDecimal(meanValue).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public static double getCollectionAvgByMetric(NoSQLSchema schema, String collectionName, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, String metricName){
        Metrics metrics = new Metrics(schema);
        
        double meanValue = 0;
        for (int i=0; i<queries.size(); i++){
            if (metricName.equalsIgnoreCase("vertex"))      meanValue += metrics.query().getVertexCoverage(collectionName, queries.get(i));
            if (metricName.equalsIgnoreCase("path"))        meanValue += metrics.query().getPathCoverage(collectionName, queries.get(i));
            if (metricName.equalsIgnoreCase("subpath"))     meanValue += metrics.query().getSubPathCoverage(collectionName, queries.get(i));
            if (metricName.equalsIgnoreCase("indpath"))     meanValue += metrics.query().getIndirectPathCoverage(collectionName, queries.get(i));
            if (metricName.equalsIgnoreCase("*path"))       meanValue += metrics.query().getInvertPathCoverage(collectionName, queries.get(i));
            if (metricName.equalsIgnoreCase("*subpath"))    meanValue += metrics.query().getInvertSubPathCoverage(collectionName, queries.get(i));
            if (metricName.equalsIgnoreCase("*indpath"))    meanValue += metrics.query().getInvertIndirectPathCoverage(collectionName, queries.get(i));
        }
        
        meanValue = meanValue / queries.size();
        return new BigDecimal(meanValue).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public static double getSchemaFinalScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        
        double finalScore = 0;        
        finalScore += getSchemaAvgByMetric(schema, queries, "vertex");
        finalScore += getSchemaScore(schema, queries);
        finalScore += getSchemaStarScore(schema, queries);
        return new BigDecimal(finalScore).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public static double getSchemaScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        
        double score = 0;        
        score += getSchemaAvgByMetric(schema, queries, "path");
        score += getSchemaAvgByMetric(schema, queries, "subpath");
        score += getSchemaAvgByMetric(schema, queries, "indpath");
        return new BigDecimal(score).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public static double getSchemaStarScore(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        
        double invScore = 0;        
        invScore += getSchemaAvgByMetric(schema, queries, "*path");
        invScore += getSchemaAvgByMetric(schema, queries, "*subpath");
        invScore += getSchemaAvgByMetric(schema, queries, "*indpath");                        
        return new BigDecimal(invScore).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public static double getCollectionFinalScore(NoSQLSchema schema, String collectionName, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        
        double finalScore = 0;        
        finalScore += getCollectionAvgByMetric(schema, collectionName, queries, "vertex");
        finalScore += getCollectionScore(schema, collectionName, queries);
        finalScore += getCollectionStarScore(schema, collectionName, queries);
        return new BigDecimal(finalScore).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public static double getCollectionScore(NoSQLSchema schema, String collectionName, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        
        double score = 0;        
        score += getCollectionAvgByMetric(schema, collectionName, queries, "path");
        score += getCollectionAvgByMetric(schema, collectionName, queries, "subpath");
        score += getCollectionAvgByMetric(schema, collectionName, queries, "indpath");
        return new BigDecimal(score).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public static double getCollectionStarScore(NoSQLSchema schema, String collectionName, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);
        
        double invScore = 0;        
        invScore += getCollectionAvgByMetric(schema, collectionName, queries, "*path");
        invScore += getCollectionAvgByMetric(schema, collectionName, queries, "*subpath");
        invScore += getCollectionAvgByMetric(schema, collectionName, queries, "*indpath");                        
        return new BigDecimal(invScore).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNÇÕES PARA FORMATAR CABEÇALHO E TABELA DE RESULTADOS.
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static String buildHeadResults(int numberOfQueries, int collectionNameMaxLenght, String text){
        // Criando cabeçalho para mostrar resultados...        
        String out = "  " + insertSpaceChars(text, collectionNameMaxLenght);
        for (int i=0; i<numberOfQueries; i++){
            out += "\tQuery"+(i+1);
        }
        return out+"\n";
    }
    
    
    public static int getCollectionNameMaxLength(ArrayList<String> collectionNames){
        // Nome padrão adicionado no cabeçalho de impressão... no mínimo será considerado esse tamanho de string.
        collectionNames.add("Collection");        
        // Recuperando o maior nome de coleção para alinhar cabeçalho.
        int maxLengthOfCollectionName = 0;
        for (String collection : collectionNames){  
            if (collection.length() > maxLengthOfCollectionName){
                maxLengthOfCollectionName = collection.length();
            }
        }
        return maxLengthOfCollectionName;
    }
    
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
