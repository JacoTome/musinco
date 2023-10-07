package com.mycompany.app;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import java.util.List;

/**
  * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String musinco = "<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/";
        Model model = ModelFactory.createDefaultModel();
        model.read("file:/home/jaco/Documents/Tirocinio/musinco/my-reasoner/src/main/java/com/mycompany/app/musinco2.rdf");
        // Create a reasoner
        String rules = "/home/jaco/Documents/Tirocinio/musinco/my-reasoner/src/main/resources/myrules.rules";
        Resource configuration =  model.createResource();
        configuration.addProperty(ReasonerVocabulary.PROPruleMode,"hybrid");
        configuration.addProperty(ReasonerVocabulary.PROPruleSet,  rules);
        List<Rule> imp_rules = Rule.rulesFromURL(rules);

// Create an instance of such a reasoner
        Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
        // Infere triples
        Model inferredModel = ModelFactory.createInfModel(reasoner, model);

        // Example SPARQL query
        String queryString =
                "PREFIX musico: <http://purl.org/ontology/musico/> " +
                "PREFIX musicoo: <http://purl.org/ontology/musico#> " +
                "SELECT * WHERE { ?subject  a musico:MusiciansGroup; musicoo:plays_genre ?g }";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, inferredModel);

        try {
            ResultSet results = qe.execSelect();
            if (!results.hasNext()) {
                System.out.println("Nothing");
            }
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                System.out.println(soln);
                // Process query results
                // ...
            }
        } finally {
            qe.close();
        }
    }
}
