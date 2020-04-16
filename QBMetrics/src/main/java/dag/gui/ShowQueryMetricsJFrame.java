/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.gui;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.nosql_schema.ConversionProcess;
import dag.nosql_schema.NoSQLSchema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import metrics.Metrics;
import static metrics.Printer.getCollectionAvgByMetric;
import static metrics.Printer.getCollectionFinalScore;
import static metrics.Printer.getCollectionNameMaxLength;
import static metrics.Printer.getCollectionScore;
import static metrics.Printer.getCollectionStarScore;
import java.util.ArrayList;
import java.util.Locale;
import org.jgrapht.graph.DirectedAcyclicGraph;
import static metrics.Printer.getQueryScore;
import static metrics.Printer.getQueryStarScore;
import static metrics.Printer.getSchemaAvgByMetric;
import static metrics.Printer.getSchemaFinalScore;
import static metrics.Printer.getSchemaScore;
import static metrics.Printer.getSchemaStarScore;
import metrics.scores.ScoreCalculator;

/**
 *
 * @author Evandro
 */
public class ShowQueryMetricsJFrame extends javax.swing.JFrame {
    private ConversionProcess conversionProcess;
    //private Metrics metrics;
    
    /**
     * Creates new form ShowQueryMetricsJFrame
     */
    public ShowQueryMetricsJFrame() {
        initComponents();
        
        
//        // Código de teste.
//        // serve para executar diretamente este formulário com um objeto ConversionProcess do disco.        
//        JSONObject jsonConversionProcess = JSONPersistence.loadJSONfromFile("D:\\conversion-process-spec\\cp-all-schemas.json");
//        // cria objeto ConversionProcesso via JSON.
//        setConversionProcess(ConversionProcessJson.fromJSON(jsonConversionProcess));
        
    }

    public void setConversionProcess(ConversionProcess conversionProcess) {
        this.conversionProcess = conversionProcess;
        loadTxtQueriesWeights();
        loadCmbFilter();
    }
    
    private void loadTxtQueriesWeights(){
        String weights = "";
        String virgula = "";
        double value = new BigDecimal( 1.0 / this.conversionProcess.getQueries().size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        for (int i=0; i<this.conversionProcess.getQueries().size(); i++){
            weights += virgula + value;
            virgula = ", ";
        }
        txtQueriesWeight.setText(weights);
    }
    
    private void loadCmbFilter(){
        cmbFilter.removeAllItems();
        cmbFilter.addItem("All");
        
        switch (cmbBy.getSelectedItem().toString()){
            case "Schema":
                for (NoSQLSchema schema : conversionProcess.getSchemas()){
                    cmbFilter.addItem(schema.getName());
                }                
                break;
                
            case "Query":
                for (int i=0; i<this.conversionProcess.getQueries().size(); i++){
                    cmbFilter.addItem("Query"+(i+1));
                }                
                break;
                
            case "Score":
                for (NoSQLSchema schema : conversionProcess.getSchemas()){
                    cmbFilter.addItem(schema.getName());
                }                
                break;
                
            case "Coverage":
                for (NoSQLSchema schema : conversionProcess.getSchemas()){
                    cmbFilter.addItem(schema.getName());
                }                
                break;
        }
    }

    private void showMetrics(){
        NoSQLSchema schema = null;
        double path_weight = Double.parseDouble(txtPathWeight.getText());
        double subpath_weight = Double.parseDouble(txtSubPathWeight.getText());
        double indpath_weight = Double.parseDouble(txtIndPathWeight.getText());
        boolean depth = chkDepthOn.isSelected();
        String queries_weights = txtQueriesWeight.getText();
        
        switch (cmbBy.getSelectedItem().toString()){
            case "Schema":
                if (cmbFilter.getSelectedItem().equals("All")){
                    String metrics = "";
                    for (NoSQLSchema s : conversionProcess.getSchemas()){
                        metrics += getMetrics_bySchema(s, conversionProcess.getQueries(), path_weight, subpath_weight, indpath_weight, depth, queries_weights) + "\n";
                    }
                    txtMetrics.setText(metrics);
                } else {
                    NoSQLSchema selectSchema = conversionProcess.getSchemaByName(cmbFilter.getSelectedItem().toString());
                    if (selectSchema != null)
                        txtMetrics.setText(getMetrics_bySchema(selectSchema, conversionProcess.getQueries(), path_weight, subpath_weight, indpath_weight, depth, queries_weights));
                }
                break;
            
            case "Query":
                if (cmbFilter.getSelectedItem().equals("All")){
                    String results = "";
                    for (int i = 1; i<cmbFilter.getItemCount(); i++){
                        results += getMetrics_byQuery(this.conversionProcess.getSchemas(), this.conversionProcess.getQueries(), this.conversionProcess.getQueries().get(i-1), i, path_weight, subpath_weight, indpath_weight, depth, queries_weights)+"\n";
                    }
                    txtMetrics.setText(results);
                } else {
                    txtMetrics.setText(getMetrics_byQuery(this.conversionProcess.getSchemas(), this.conversionProcess.getQueries(), this.conversionProcess.getQueries().get(cmbFilter.getSelectedIndex()-1),cmbFilter.getSelectedIndex(), path_weight, subpath_weight, indpath_weight, depth, queries_weights));
                }
                break;
                
            case "Score":
                if (cmbFilter.getSelectedItem().equals("All")){
                    String metrics = getSScores_bySchema(conversionProcess.getSchemas(), conversionProcess.getQueries(), path_weight, subpath_weight, indpath_weight, depth, queries_weights) + "\n";
                    txtMetrics.setText(metrics);
                } else {
                    ArrayList<NoSQLSchema> array_with_one_schema = new ArrayList<>();
                    array_with_one_schema.add(conversionProcess.getSchemaByName(cmbFilter.getSelectedItem().toString()));
                    txtMetrics.setText(getSScores_bySchema(array_with_one_schema, conversionProcess.getQueries(), path_weight, subpath_weight, indpath_weight, depth, queries_weights));
                }
                break;
                
            case "Coverage":
                if (cmbFilter.getSelectedItem().equals("All")){
                    String metrics = "";
                    for (NoSQLSchema s : conversionProcess.getSchemas()){
                        metrics += getCoverage_bySchema(s, conversionProcess.getQueries()) + "\n";
                    }
                    txtMetrics.setText(metrics);
                } else {
                    NoSQLSchema selectSchema = conversionProcess.getSchemaByName(cmbFilter.getSelectedItem().toString());
                    if (selectSchema != null)
                        txtMetrics.setText(getCoverage_bySchema(selectSchema, conversionProcess.getQueries()));
                }
                break;
        }
        txtMetrics.setCaretPosition(0);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // (1) - METRICS by SCHEMAS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getMetrics_bySchema(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, double path_weight, double subpath_weight, double indpath_weight, boolean depth, String queries_weights){
        Metrics metrics = new Metrics(schema);
        
        ArrayList<Double> queriesWeights = new ArrayList();
        for (int i=0; i<queries.size(); i++){
            queriesWeights.add( Double.parseDouble( queries_weights.split(",")[i] ) );
        }
        
        ScoreCalculator scoreCalc = new ScoreCalculator(schema, queries, path_weight, subpath_weight, indpath_weight, depth, queriesWeights);
        
        String output = "";
        output += "Schema Name: " + schema.getName();
        output += "\nSet of\t\tCoverage\t\t\t\t\t\t| QScores";
        
        
        // 1 COVERAGE IN SCHEMA
        String header = "Queries\t\t";
        header += "Path (D)   \t"; 
        header += "SubPath (D)\t"; 
        header += "IndPath (D)\t";
        header += "DirEdge \t";
        header += "AllEdge \t";
        header += "ReqColls\t";
        header += "| ";
        header += "Paths    \t"; 
        header += "DirEdge \t";
        header += "AllEdge \t";
        header += "ReqColls\t";        
        output += "\n"+header+"\n";
        output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        
        for (int i=0; i<queries.size(); i++){
            String out = "\n"+(i+1)+"\t\t";
            int d = metrics.queryPath().getMaxSchemaCoverage(queries.get(i)).getDepth();
            out += metrics.queryPath().getMaxSchemaCoverage(queries.get(i)).getValue()+" ("+d+")  \t";
            d = metrics.querySubPath().getMaxSchemaCoverage(queries.get(i)).getDepth();
            out += metrics.querySubPath().getMaxSchemaCoverage(queries.get(i)).getValue()+" ("+d+")  \t";
            d = metrics.queryIndirectPath().getMaxSchemaCoverage(queries.get(i)).getDepth();
            out += metrics.queryIndirectPath().getMaxSchemaCoverage(queries.get(i)).getValue()+" ("+d+")  \t";
            out += metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(queries.get(i)).getValue()+"      \t";
            out += metrics.queryEdge().getMaxSchemaAllEdgeCoverage(queries.get(i)).getValue()+"      \t";
            out += metrics.queryJoin().getMaxSchemaCoverage(queries.get(i), false).getNumberOfCollection()+"      \t";            
            out += "| ";
            out += scoreCalc.calcQScorePaths(i)+"      \t";
            out += scoreCalc.calcQScoreDirEdge(i)+"      \t";
            out += scoreCalc.calcQScoreAllEdge(i)+"      \t";
            out += scoreCalc.calcQScoreReqColls(i)+"      \t";
            output += out;
        }
        output += "\n";
        output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\n\t\t\t\t\t\t\tSScore:\t| ";
        output += String.format(Locale.US, "%.2f", scoreCalc.calcSScorePaths())+"      \t";
        output += String.format(Locale.US, "%.2f", scoreCalc.calcSScoreDirEdge())+"      \t";
        output += String.format(Locale.US, "%.2f", scoreCalc.calcSScoreAllEdge())+"      \t";
        output += String.format(Locale.US, "%.2f", scoreCalc.calcSScoreReqColls())+"      \t";
        return output;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // (2) - Metrics by QUERIES
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    public static String getMetrics_bySchema(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, double path_weight, double subpath_weight, double indpath_weight, boolean depth, String queries_weights){
    public static String getMetrics_byQuery (ArrayList<NoSQLSchema> schemas, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, int queryIndex, double path_weight, double subpath_weight, double indpath_weight, boolean depth, String queries_weights){        
        String output = "";
        output += "Query: " + queryIndex +" - "+ new Metrics(null).path().getPaths(query);;
        output += "\nSet of\t\tCoverage\t\t\t\t\t\t| QScores";
        
        // 1 COVERAGE IN SCHEMA, by QUERY
        String header = "Schema\t\t";
        header += "Path (D)   \t"; 
        header += "SubPath (D)\t"; 
        header += "IndPath (D)\t";
        header += "DirEdge \t";
        header += "AllEdge \t";
        header += "ReqColls\t";
        header += "| ";
        header += "Paths    \t"; 
        header += "DirEdge \t";
        header += "AllEdge \t";
        header += "ReqColls\t";        
        output += "\n"+header+"\n";
        output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        
//        double path_weight = Double.parseDouble(txtPathWeight.getText());
//        double subpath_weight = Double.parseDouble(txtSubPathWeight.getText());
//        double indpath_weight = Double.parseDouble(txtIndPathWeight.getText());
//        boolean depth = chkDepthOn.isSelected();
        
        ArrayList<Double> queriesWeights = new ArrayList();
        for (int i=0; i<queries.size(); i++){
//            queriesWeights.add( Double.parseDouble( txtQueriesWeight.getText().split(",")[i] ) );
            queriesWeights.add( Double.parseDouble( queries_weights.split(",")[i] ) );
        }
        
        for (NoSQLSchema schema : schemas){
            Metrics metrics = new Metrics(schema);
            ScoreCalculator scoreCalc = new ScoreCalculator(schema, queries, path_weight, subpath_weight, indpath_weight, depth, queriesWeights);
            
            String out = "\n"+schema.getName()+"\t\t";
            int d = metrics.queryPath().getMaxSchemaCoverage(query).getDepth();
            out += metrics.queryPath().getMaxSchemaCoverage(query).getValue()+" ("+d+")  \t";
            d = metrics.querySubPath().getMaxSchemaCoverage(query).getDepth();
            out += metrics.querySubPath().getMaxSchemaCoverage(query).getValue()+" ("+d+")  \t";
            d = metrics.queryIndirectPath().getMaxSchemaCoverage(query).getDepth();
            out += metrics.queryIndirectPath().getMaxSchemaCoverage(query).getValue()+" ("+d+")  \t";
            out += metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(query).getValue()+"      \t";
            out += metrics.queryEdge().getMaxSchemaAllEdgeCoverage(query).getValue()+"      \t";
            out += metrics.queryJoin().getMaxSchemaCoverage(query, false).getNumberOfCollection()+"      \t";            
            out += "| ";
            out += scoreCalc.calcQScorePaths(queryIndex-1)+"      \t";
            out += scoreCalc.calcQScoreDirEdge(queryIndex-1)+"      \t";
            out += scoreCalc.calcQScoreAllEdge(queryIndex-1)+"      \t";
            out += scoreCalc.calcQScoreReqColls(queryIndex-1)+"      \t";
            output += out;
        }
        output += "\n";
        output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        return output;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // (3) - SSCORES by SCHEMAS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getSScores_bySchema(ArrayList<NoSQLSchema> schemas, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, double path_weight, double subpath_weight, double indpath_weight, boolean depth, String queries_weights){
        
        ArrayList<Double> queriesWeights = new ArrayList();
        for (int i=0; i<queries.size(); i++){
            queriesWeights.add( Double.parseDouble( queries_weights.split(",")[i] ) );
        }
        
        String output = "";
        output += "SScore by Schema:\n";
        
        // 1 COVERAGE IN SCHEMA
        String header = "Schemas\t\t";
        header += "Paths    \t"; 
        header += "DirEdge \t";
        header += "AllEdge \t";
        header += "ReqColls\t";        
        output += header+"\n";
        output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        
        for (NoSQLSchema schema : schemas){
            ScoreCalculator scoreCalc = new ScoreCalculator(schema, queries, path_weight, subpath_weight, indpath_weight, depth, queriesWeights);            
            String out = "\n" + schema.getName() + "\t\t";
            out += String.format(Locale.US, "%.2f", scoreCalc.calcSScorePaths())+"      \t";
            out += String.format(Locale.US, "%.2f", scoreCalc.calcSScoreDirEdge())+"      \t";
            out += String.format(Locale.US, "%.2f", scoreCalc.calcSScoreAllEdge())+"      \t";
            out += String.format(Locale.US, "%.2f", scoreCalc.calcSScoreReqColls())+"      \t";
            output += out;
        }
        output += "\n";
        output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        return output;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // (4) - COVERAGE by SCHEMAS
    public String getCoverage_bySchema(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){
        Metrics metrics = new Metrics(schema);

        String listOfMetrics[] = {"Path", "SubPath", "IndPath", "DirEdge", "AllEdge", "ReqColls"};
        String output = "";
        
        for (String metricName : listOfMetrics){
            output += "Schema Name: " + schema.getName();
            output += "\nMetric: " + metricName;

            // 1 COVERAGE IN SCHEMA
            String header = "Queries\t\t";
            header += "MaxCov   \t"; 
            header += "MaxDepth \t"; 
            header += "Detailed Coverage by Collection\t";
            output += "\n"+header+"\n";
            output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";

            for (int i=0; i<queries.size(); i++){
                DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = queries.get(i);

                String out = "\n"+(i+1)+"\t\t";
                switch (metricName){
                    case "Path":
                        out += metrics.queryPath().getMaxSchemaCoverage(query).getValue()+"      \t";
                        out += metrics.queryPath().getMaxSchemaCoverage(query).getDepth()+"      \t";
                        out += metrics.queryPath().getSchemaCoverage(query)+"      \t";
                        break;
                    case "SubPath":
                        out += metrics.querySubPath().getMaxSchemaCoverage(query).getValue()+"      \t";
                        out += metrics.querySubPath().getMaxSchemaCoverage(query).getDepth()+"      \t";
                        out += metrics.querySubPath().getSchemaCoverage(query)+"      \t";
                        break;
                    case "IndPath":
                        out += metrics.queryIndirectPath().getMaxSchemaCoverage(query).getValue()+"      \t";
                        out += metrics.queryIndirectPath().getMaxSchemaCoverage(query).getDepth()+"      \t";
                        out += metrics.queryIndirectPath().getSchemaCoverage(query)+"      \t";
                        break;
                    case "DirEdge":
                        out += metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(query).getValue()+"      \t";
                        out += metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(query).getDepth()+"      \t";
                        out += metrics.queryEdge().getSchemaDirectEdgeCoverage(query)+"      \t";
                        break;
                    case "AllEdge":
                        out += metrics.queryEdge().getMaxSchemaAllEdgeCoverage(query).getValue()+"      \t";
                        out += metrics.queryEdge().getMaxSchemaAllEdgeCoverage(query).getDepth()+"      \t";
                        out += metrics.queryEdge().getSchemaAllEdgeCoverage(query)+"      \t";
                        break;
//                    case "ReqColls":
//                        out += metrics.queryJoin().getMaxSchemaCoverage(query, false).getNumberOfCollection()+"      \t";
//                        out += "-" + "      \t";
//                        out += metrics.queryJoin().getSchemaCoverage(query, false)+"      \t";
//                        break;
                        // ESTA OPÇÃO TEM QUE SER MELHOR FORMATADA, MAS ESTÁ FUNCIONANDO!
                }
                
                output += out;
            }
            output += "\n";
            output += "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
            output += "\n";
        }
        return output;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // METODOS ANTIGOS, USANDO NA ANTIGA VERSÃO DO FORMULÁRIOS...
    // NO FUTURO, REMOVER DAQUI!!!
    
    public String query_x_metric_groupBy_schema(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        String output = "";
        Metrics metrics = new Metrics(schema);    
        int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
                
        output += "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nSchema Name: " + schema.getName();
        
        // 1 COVERAGE IN SCHEMA
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
        output += "\n"+header;        
        
        for (int i=0; i<queries.size(); i++){
            String out = i+1+"\t\t";
            out += metrics.query().getVertexCoverageInSchema(queries.get(i))+"      \t";    
            out += metrics.query().getPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getSubPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getIndirectPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getInvertPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getInvertSubPathCoverageInSchema(queries.get(i))+"      \t";
            out += metrics.query().getInvertIndirectPathCoverageInSchema(queries.get(i))+"      \t";            
            out += getQueryScore(metrics, queries.get(i))+"      \t";
            out += getQueryStarScore(metrics, queries.get(i));
            output += "\n"+out;
        } 
        
        // Percorre o vetor METRICS e calcula o valor médio de cada métrica para todo o conjunto de consultas.
        output += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nAvg\t\t";
        for (int i=0; i<Metrics.METRICS.length; i++){
            output += getSchemaAvgByMetric(schema, queries, Metrics.METRICS[i]) + "      \t";
        }
        
        // Imprimindo Score Final do Esquema (Somatório do valor médio de cada métrica).        
        output += "\nScore      \t\t"+ getSchemaScore(schema, queries);
        output += "\nScore*     \t\t"+ getSchemaStarScore(schema, queries);
        output += "\nFinal Score\t\t"+ getSchemaFinalScore(schema, queries);
        
        return output;
    }
    
    public String query_x_schemas_groupBy_metric(ArrayList<NoSQLSchema> schemas, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, String metric){        
        String output = "";            
        //int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
                
        output += "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nMetric: " + metric;
        
        String header = "Queries\t\t";
        for (NoSQLSchema schema : schemas){
            header += schema.getName() +"\t";
        }
        output += "\n"+header;
        
        for (int i=0; i<queries.size(); i++){
            String out = i+1+"\t\t";
            for (NoSQLSchema schema : schemas){
                Metrics metrics = new Metrics(schema);
                if (metric.equalsIgnoreCase("vertex"))  out += metrics.query().getVertexCoverageInSchema(queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("path"))    out += metrics.query().getPathCoverageInSchema(queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("subpath")) out += metrics.query().getSubPathCoverageInSchema(queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("indpath")) out += metrics.query().getIndirectPathCoverageInSchema(queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("*path"))   out += metrics.query().getInvertPathCoverageInSchema(queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("*subpath"))out += metrics.query().getInvertSubPathCoverageInSchema(queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("*indpath"))out += metrics.query().getInvertIndirectPathCoverageInSchema(queries.get(i))+"      \t";
            }
            output += "\n"+out;
        }
        
        // Calculando média da métrica para cada esquema
        output += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nAvg\t\t";
        for (NoSQLSchema schema : schemas){
            output += getSchemaAvgByMetric(schema, queries, metric)+"\t";
        }
        
        return output;
    }
    
    public String schemas_x_metrics_groupBy_query(ArrayList<NoSQLSchema> schemas, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, int queryIndex){        
        String output = "";            
        //int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
                
        output += "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nQuery: " + queryIndex +" - "+ new Metrics(null).path().getPaths(query);
        
        String header = "Schema\t\t";
        header += "Vertex  \t"; 
        header += "Path    \t"; 
        header += "SubPath \t"; 
        header += "IndPath \t";
        header += "*Path   \t";
        header += "*SubPath\t";
        header += "*IndPath\t";
        header += "QScore  \t";
        header += "*QScore \t";
        output += "\n"+header;  
        
        // Média do valor da métrica selecionada para cada esquema.
        double[] meanValues = new double[schemas.size()];
        
        for (NoSQLSchema schema : schemas){
            Metrics metrics = new Metrics(schema);
            
            String out = schema.getName()+"\t\t";
            out += metrics.query().getVertexCoverageInSchema(query)+"      \t";    
            out += metrics.query().getPathCoverageInSchema(query)+"      \t";
            out += metrics.query().getSubPathCoverageInSchema(query)+"      \t";
            out += metrics.query().getIndirectPathCoverageInSchema(query)+"      \t";
            out += metrics.query().getInvertPathCoverageInSchema(query)+"      \t";
            out += metrics.query().getInvertSubPathCoverageInSchema(query)+"      \t";
            out += metrics.query().getInvertIndirectPathCoverageInSchema(query)+"      \t";            
            out += getQueryScore(metrics, query)+"      \t";
            out += getQueryStarScore(metrics, query);
            output += "\n"+out;
        }
        return output;
    }
    
    public String schemas_x_avg_metrics(ArrayList<NoSQLSchema> schemas, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        String output = "";            
        //int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
                
        output += "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nAll Schemas Sorted By FScore:";
        output += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        
        String header = "Schema\t\t";
        header += "Vertex  \t"; 
        header += "Path    \t"; 
        header += "SubPath \t"; 
        header += "IndPath \t";
        header += "*Path   \t";
        header += "*SubPath\t";
        header += "*IndPath\t";
        header += "Score  \t";
        header += "*Score \t";
        header += "FScore \t";
        output += "\n"+header;  
       
        for (NoSQLSchema schema : schemas){
            Metrics metrics = new Metrics(schema);
            
            String out = schema.getName()+"\t\t";
            out += getSchemaAvgByMetric(schema, queries, "vertex")+"      \t";
            out += getSchemaAvgByMetric(schema, queries, "path")+"      \t";
            out += getSchemaAvgByMetric(schema, queries, "subpath")+"      \t";
            out += getSchemaAvgByMetric(schema, queries, "indpath")+"      \t";
            out += getSchemaAvgByMetric(schema, queries, "*path")+"      \t";
            out += getSchemaAvgByMetric(schema, queries, "*subpath")+"      \t";
            out += getSchemaAvgByMetric(schema, queries, "*indpath")+"      \t";
            out += getSchemaScore(schema, queries)+"      \t";
            out += getSchemaStarScore(schema, queries)+"      \t";
            out += getSchemaFinalScore(schema, queries)+"      \t";            
            output += "\n"+out;
        }
        return output;
    }
    
    public String query_x_metric_groupBy_schema_collection(NoSQLSchema schema, String collectionName, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries){        
        String output = "";
        Metrics metrics = new Metrics(schema);    
        int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
                
        output += "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nSchema: " + schema.getName() + " / Collection: " + collectionName;
        
        // 1 COVERAGE IN SCHEMA/COLLECTION
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
        output += "\n"+header;        
        
        for (int i=0; i<queries.size(); i++){
            String out = i+1+"\t\t";
            out += metrics.query().getVertexCoverage(collectionName, queries.get(i))+"      \t";    
            out += metrics.query().getPathCoverage(collectionName, queries.get(i))+"      \t";
            out += metrics.query().getSubPathCoverage(collectionName, queries.get(i))+"      \t";
            out += metrics.query().getIndirectPathCoverage(collectionName, queries.get(i))+"      \t";
            out += metrics.query().getInvertPathCoverage(collectionName, queries.get(i))+"      \t";
            out += metrics.query().getInvertSubPathCoverage(collectionName, queries.get(i))+"      \t";
            out += metrics.query().getInvertIndirectPathCoverage(collectionName, queries.get(i))+"      \t";            
            out += getQueryScore(collectionName, metrics, queries.get(i))+"      \t";
            out += getQueryStarScore(collectionName, metrics, queries.get(i));
            output += "\n"+out;
        } 
        
        // Percorre o vetor METRICS e calcula o valor médio de cada métrica para todo o conjunto de consultas.
        output += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nAvg\t\t";
        for (int i=0; i<Metrics.METRICS.length; i++){
            output += getCollectionAvgByMetric(schema, collectionName, queries, Metrics.METRICS[i]) + "      \t";
        }
        
        // Imprimindo Score Final da Coleção (Somatório do valor médio de cada métrica).        
        output += "\nScore      \t\t"+ getCollectionScore(schema, collectionName, queries);
        output += "\nScore*     \t\t"+ getCollectionStarScore(schema, collectionName, queries);
        output += "\nCol. Score\t\t"+ getCollectionFinalScore(schema, collectionName, queries);
        
        return output;
    }
    
    public String collection_x_metrics_groupBy_schema_query(NoSQLSchema schema, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, int queryIndex){        
        String output = "";            
        //int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
        Metrics metrics = new Metrics(schema);
                
        output += "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nSchema: " + schema.getName() +" / Query: " + queryIndex + " - " + metrics.path().getPaths(query);
        
        String header = "Collection\t\t";
        header += "Vertex  \t"; 
        header += "Path    \t"; 
        header += "SubPath \t"; 
        header += "IndPath \t";
        header += "*Path   \t";
        header += "*SubPath\t";
        header += "*IndPath\t";
        header += "QScore  \t";
        header += "*QScore \t";
        output += "\n"+header;  

        for (int i=0; i<schema.getEntities().size(); i++){
            String collectionName = schema.getEntityName(i);
            
            String out = collectionName+"\t\t";
            out += metrics.query().getVertexCoverage(collectionName, query)+"      \t";    
            out += metrics.query().getPathCoverage(collectionName, query)+"      \t";
            out += metrics.query().getSubPathCoverage(collectionName, query)+"      \t";
            out += metrics.query().getIndirectPathCoverage(collectionName, query)+"      \t";
            out += metrics.query().getInvertPathCoverage(collectionName, query)+"      \t";
            out += metrics.query().getInvertSubPathCoverage(collectionName, query)+"      \t";
            out += metrics.query().getInvertIndirectPathCoverage(collectionName, query)+"      \t";            
            out += getQueryScore(collectionName, metrics, query)+"      \t";
            out += getQueryStarScore(collectionName, metrics, query);
            output += "\n"+out;
        }
        return output;
    }
    
    public String query_x_collection_groupBy_schema_metric(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, String metric){        
        String output = "";            
        //int collectionNameMaxLenght = getCollectionNameMaxLength(metrics.getListOfCollectionsInSchema());
        Metrics metrics = new Metrics(schema);        
        
        output += "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nSchema: " + schema.getName() + " / Metric: " + metric;
        
        String header = "Queries\t\t";
        for (int i=0; i<schema.getEntities().size(); i++){
            String collectionName = schema.getEntityName(i);
            header += collectionName +"\t";
        }
        output += "\n"+header;
                
        for (int i=0; i<queries.size(); i++){
            String out = i+1+"\t\t";
            for (int j=0; j<schema.getEntities().size(); j++){
                String collectionName = schema.getEntityName(j);
                
                if (metric.equalsIgnoreCase("vertex"))  out += metrics.query().getVertexCoverage(collectionName, queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("path"))    out += metrics.query().getPathCoverage(collectionName, queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("subpath")) out += metrics.query().getSubPathCoverage(collectionName, queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("indpath")) out += metrics.query().getIndirectPathCoverage(collectionName, queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("*path"))   out += metrics.query().getInvertPathCoverage(collectionName, queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("*subpath"))out += metrics.query().getInvertSubPathCoverage(collectionName, queries.get(i))+"      \t";
                if (metric.equalsIgnoreCase("*indpath"))out += metrics.query().getInvertIndirectPathCoverage(collectionName, queries.get(i))+"      \t";
            }
            output += "\n"+out;
        }
        
        // Percorre o vetor METRICS e calcula o valor médio de cada métrica para todo o conjunto de consultas.
        output += "\n-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        output += "\nAvg\t\t";
        for (int i=0; i<schema.getEntities().size(); i++){
            output += getCollectionAvgByMetric(schema, schema.getEntityName(i), queries, metric) + "      \t";
        }        
        return output;
    }
    
    
    // PENSAR AONDE COLOCAR ESSES MÉTODOS DE CALCULOS GENÉRICOS
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbBy = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        cmbFilter = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMetrics = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        txtPathWeight = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtSubPathWeight = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtIndPathWeight = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtQueriesWeight = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        chkDepthOn = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Query Metrics");
        setExtendedState(6);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));

        jLabel1.setText("By:");

        cmbBy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Schema", "Query", "Score", "Coverage" }));
        cmbBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbByActionPerformed(evt);
            }
        });

        jLabel2.setText("Value:");

        cmbFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbBy, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)))
        );

        jScrollPane1.setViewportView(txtMetrics);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Weights"));

        txtPathWeight.setText("1.0");

        jLabel3.setText("Path:");

        jLabel4.setText("SubPath:");

        txtSubPathWeight.setText("0.7");

        jLabel5.setText("IndPath:");

        txtIndPathWeight.setText("0.5");

        jLabel6.setText("Queries:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtPathWeight, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSubPathWeight, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIndPathWeight, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtQueriesWeight))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPathWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtSubPathWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtIndPathWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtQueriesWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Penalize Depth"));

        chkDepthOn.setSelected(true);
        chkDepthOn.setText("On");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkDepthOn)
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(chkDepthOn)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterActionPerformed
        if (cmbFilter.getItemCount()>0) showMetrics();
    }//GEN-LAST:event_cmbFilterActionPerformed

    private void cmbByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbByActionPerformed
        loadCmbFilter();
    }//GEN-LAST:event_cmbByActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ShowQueryMetricsJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ShowQueryMetricsJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ShowQueryMetricsJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ShowQueryMetricsJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ShowQueryMetricsJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkDepthOn;
    private javax.swing.JComboBox<String> cmbBy;
    private javax.swing.JComboBox<String> cmbFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtIndPathWeight;
    private javax.swing.JTextPane txtMetrics;
    private javax.swing.JTextField txtPathWeight;
    private javax.swing.JTextField txtQueriesWeight;
    private javax.swing.JTextField txtSubPathWeight;
    // End of variables declaration//GEN-END:variables
}
