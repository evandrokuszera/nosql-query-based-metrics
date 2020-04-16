/*
 * Está classe tem por objetivo calcular scores, sendo:
 *   QSCORE
 *   SSCORE
 *
 * Observações:
    - QScore:
      - Para as métricas path, subpath e indpath
        - considera o peso de cada tipo de caminho.
        - considera a profundidade do caminho.
      - Para as demais métricas, o QScore é o mesmo valor retornado do cálculo de cobertura (Coverage).      
    - SScore:
      - SScorePaths representa o SScore para as métricas path, subpath e indpath (ver QScore).
      - Para as demais métricas há um SScore específico.
      - Peso das consultas é considerado no cálculo de SScore, exceto para a métrica ReqColls.
 *
 */
package metrics.scores;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.nosql_schema.NoSQLSchema;
import metrics.Metrics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class ScoreCalculator {
    private double path_weight;
    private double subpath_weight;
    private double indpath_weight;
    private boolean calcWithDepth;
    private ArrayList<Double> queries_weights = new ArrayList<>();
    private NoSQLSchema schema;
    private ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries;
    private Metrics metrics;

    // Construtor com todos os parâmetros necessários.
    public ScoreCalculator(NoSQLSchema schema,
                            ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries, 
                            double path_weight, 
                            double subpath_weight, 
                            double indpath_weight, 
                            boolean calcWithDepth, 
                            ArrayList<Double> queries_weights) {
        
        this.path_weight = path_weight;
        this.subpath_weight = subpath_weight;
        this.indpath_weight = indpath_weight;
        this.calcWithDepth = calcWithDepth;
        this.schema = schema;
        this.queries = queries;
        this.queries_weights = queries_weights;
        this.metrics = new Metrics(schema);
    }
    
    // Construtor que configura pesos padrão para path, subpath, indpath e, mesmo peso para as consultas.
    public ScoreCalculator(NoSQLSchema schema, ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries) {
        this(schema, queries, 1.0, 0.7, 0.5, true, null);
        
        // como o usuário não definiu os pesos para as consultas, o código abaixo define que cada consulta terá o mesmo peso, baseado no número de consultas.
        queries_weights = new ArrayList<>();
        double weight = new BigDecimal( 1.0 / queries.size() ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        for (int i=0; i<queries.size(); i++){
            queries_weights.add(weight);
        }
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SSCORE:
    // Métodos para calcular o SScore (Schema Score), para cada métrica. Exemplos: calcSScorePaths, calcSScoreDirEdge, ...
    // Entrada: conjunto de consultas e um esquema.
    // Saída: valor do SScore (Schema Score).
    // Considera: considera o valor de QScore e peso das consultas.
    
    public double calcSScorePaths(){
        double score = 0.0;
        for (int queryIndex=0; queryIndex<queries.size(); queryIndex++){  
            double value = calcQScorePaths(queryIndex) * queries_weights.get(queryIndex);
            score += value;
        }
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    public double calcSScoreInvPaths(){
        double score = 0.0;
        for (int queryIndex=0; queryIndex<queries.size(); queryIndex++){  
            double value = calcQScoreInvPaths(queryIndex) * queries_weights.get(queryIndex);
            score += value;
        }
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    public double calcSScoreDirEdge(){
        double score = 0.0;
        for (int queryIndex=0; queryIndex<queries.size(); queryIndex++){  
            double value = calcQScoreDirEdge(queryIndex) * queries_weights.get(queryIndex);
            score += value;
        }
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    public double calcSScoreAllEdge(){
        double score = 0.0;
        for (int queryIndex=0; queryIndex<queries.size(); queryIndex++){  
            double value = calcQScoreAllEdge(queryIndex) * queries_weights.get(queryIndex);
            score += value;
        }
        score = new BigDecimal( score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    public double calcSScoreReqColls(){
        double score = 0.0;
        for (int queryIndex=0; queryIndex<queries.size(); queryIndex++){  
            double value = calcQScoreReqColls(queryIndex); // * queries_weights.get(queryIndex); não uso pesos para esta métrica
            score += value;
        }
        score = new BigDecimal( queries.size() / score ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();    
        return score;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // QSCORE:
    // Método para calcular o QScore para os Paths (inclui path, subpath e indpath metric values)
    // Entrada: uma consulta e um esquema.
    // Saída: valor do QScore (Query Score).
    // Considera: cobertura da consulta no esquema, pesos dos paths e profundidade onde foi localizado o caminho.
    
    public double calcQScorePaths(int queryIndex){
        // Calculando o valor da métrica, considerando pesos e profundidade dos caminhos.
        double path    = calcPathMetricValue(queryIndex);
        double subpath = calcSubPathMetricValue(queryIndex);
        double indpath = calcIndPathMetricValue(queryIndex);
        // QScore é igual ao valor máximo entre path, subpath e indpath, conforme descrito no artigo.
        double maxvalue = Math.max(path, Math.max(subpath, indpath));
        return maxvalue;
    }
    
    public double calcQScoreInvPaths(int queryIndex){
        // Calculando o valor da métrica, considerando pesos e profundidade dos caminhos.
        double path    = calcInvPathMetricValue(queryIndex);
        double subpath = calcInvSubPathMetricValue(queryIndex);
        double indpath = calcInvIndPathMetricValue(queryIndex);
        // QScore é igual ao valor máximo entre path, subpath e indpath, conforme descrito no artigo.
        double maxvalue = Math.max(path, Math.max(subpath, indpath));
        return maxvalue;
    }
    
    public double calcQScoreDirEdge(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        // QScore para DirEdge é o mesmo valor da métrica DirEgdeCoverage
        return metrics.queryEdge().getMaxSchemaDirectEdgeCoverage(query).getValue();
    }
    
    public double calcQScoreAllEdge(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        // QScore para AllEdge é o mesmo valor da métrica AllEgdeCoverage
        return metrics.queryEdge().getMaxSchemaAllEdgeCoverage(query).getValue();
    }
    
    public int calcQScoreReqColls(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        // QScore para ReqColls é o mesmo valor da métrica ReqColsCoverage (ainda não alterei o nome Join para ReqColls).
        return metrics.queryJoin().getMaxSchemaCoverage(query, false).getNumberOfCollection();
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Métodos para calcular o valor de uma métrica (PATH, SUBPATH, INDPATH)
    // Entrada: uma consulta e um esquema.
    // Saída: valor da métrica.
    // Considera no cálculo:
    //  - Cobertura máxima da consulta sobre o esquema e
    //  - Pesos dos caminhos (path, subpath e indpath) e 
    //  - Profundidade onde foi localizado o caminho (para penalização).
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private double calcInvPathMetricValue(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        double path = path_weight * metrics.queryPath().getInvMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryPath().getInvMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            path /= depth;
        }
        return new BigDecimal( path ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    private double calcInvSubPathMetricValue(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        double subpath = subpath_weight * metrics.querySubPath().getInvMaxSchemaCoverage(query).getValue();
        int depth = metrics.querySubPath().getInvMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            subpath /= depth;
        }
        return new BigDecimal( subpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    private double calcInvIndPathMetricValue(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        double indpath = indpath_weight * metrics.queryIndirectPath().getInvMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryIndirectPath().getInvMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            indpath /= depth;
        }
        return new BigDecimal( indpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    private double calcPathMetricValue(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        double path = path_weight * metrics.queryPath().getMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryPath().getMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            path /= depth;
        }
        return new BigDecimal( path ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    private double calcSubPathMetricValue(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        double subpath = subpath_weight * metrics.querySubPath().getMaxSchemaCoverage(query).getValue();
        int depth = metrics.querySubPath().getMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            subpath /= depth;
        }
        return new BigDecimal( subpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    private double calcIndPathMetricValue(int queryIndex){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> query = this.queries.get(queryIndex);
        
        double indpath = indpath_weight * metrics.queryIndirectPath().getMaxSchemaCoverage(query).getValue();
        int depth = metrics.queryIndirectPath().getMaxSchemaCoverage(query).getDepth();
        // usando a profundidade para calcular o valor da métrica, quanto mais profundo, pior!
        if (calcWithDepth && depth > 0){ 
            indpath /= depth;
        }
        return new BigDecimal( indpath ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
}