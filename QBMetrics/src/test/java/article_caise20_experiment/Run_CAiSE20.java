//CAiSE 20' Experiment.
// This class has the experiments executed for the CAiSE article.
// All the schemas and queries are encapsulated in a ConversionProcess, located at Project Resource Package.

package article_caise20_experiment;

import dag.gui.ShowQueryMetricsJFrame;
import dag.nosql_schema.ConversionProcess;
import dag.nosql_schema.NoSQLSchema;
import dag.persistence.ConversionProcessJson;
import dag.persistence.JSONPersistence;
import java.io.File;
import java.net.URISyntaxException;
import org.json.JSONObject;

public class Run_CAiSE20 {

    public static void main(String[] args) {
        System.out.println("CAiSE 2020 EXPERIMENTS\n");
        
        ConversionProcess conversionProcess = loadExperimentConversionProcess();
        double path_weight = 1.0;
        double subpath_weight = 0.5;
        double indpath_weight = 0.3;
        boolean useDepth = true;
        String queries_weights = "0.14, 0.14, 0.14, 0.14, 0.14, 0.14, 0.14";
        
        System.out.println("1 - METRIC RESULTS BY SCHEMA:");
        for (NoSQLSchema schema : conversionProcess.getSchemas()){
            String results = ShowQueryMetricsJFrame.getMetrics_bySchema(schema, conversionProcess.getQueries(), path_weight, subpath_weight, indpath_weight, true, queries_weights);
            System.out.println(results);
        }
        
        System.out.println("\n\n2 - METRIC RESULTS BY QUERY:");
        for (int i=0; i<conversionProcess.getQueries().size(); i++){
            String results = ShowQueryMetricsJFrame.getMetrics_byQuery(conversionProcess.getSchemas(), conversionProcess.getQueries(), conversionProcess.getQueries().get(i), i+1, path_weight, subpath_weight, indpath_weight, useDepth, queries_weights);
            System.out.println(results);
        }
        
        System.out.println("\n\n3 - SScore RESULTS BY SCHEMA:");
        String results = ShowQueryMetricsJFrame.getSScores_bySchema(conversionProcess.getSchemas(), conversionProcess.getQueries(), path_weight, subpath_weight, indpath_weight, true, queries_weights);
        System.out.println(results);
    }
    
    private static ConversionProcess loadExperimentConversionProcess(){
        String conversionProcessFilePath = "/caise20/main_article_conversion_process.json";        
        ConversionProcess conversionProcess = null;        
        try {
            File f = new File(new Run_CAiSE20().getClass().getResource(conversionProcessFilePath).toURI());
            JSONObject jsonConversionProcess = JSONPersistence.loadJSONfromFile(f.getAbsolutePath());
            conversionProcess = ConversionProcessJson.fromJSON(jsonConversionProcess);
        } catch (URISyntaxException ex) {
            System.out.println("Error opening the '"+conversionProcessFilePath+"' file.");
        }
        return conversionProcess;
    }
}
