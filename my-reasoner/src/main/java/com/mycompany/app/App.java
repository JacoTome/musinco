package com.mycompany.app;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

public class App {

    public static final String MUSINCO = "src/main/resources/musinco4.rdf";
    public static final String MUSICO = "src/main/resources/MUSICO.rdf";
    public static final String DATA = "src/main/resources/musinco4-materialized.xml";
    public static final String RULES = "src/main/resources/myrules.rules";


    public static FusekiServer startServer(Dataset ds) {
        FusekiLogging.setLogging();
        return FusekiServer.create()
                .verbose(true)
                .add("/ds", ds)
                .port(3030)
                .build();
    }

    public static void main(String[] args) {

        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.read(MUSINCO, "RDF/XML");
        ontModel.read(MUSICO, "RDF/XML");
        ontModel.read(DATA, "RDF/XML");
        OntModel ontModelInf = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, ontModel);
        System.out.println("Ontology loaded");

        Model baseInf = ontModelInf.getBaseModel();
        System.out.println("Base model created");

        // Create a reasoner
        Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL(RULES));
        System.out.println("Reasoner created");

        // Create an inference model
        InfModel inf = ModelFactory.createInfModel(reasoner, baseInf);
        System.out.println("Inference model created");

        // create Dataset from inf model
        Dataset ds = DatasetFactory.create(inf);
        System.out.println("Dataset created");
        FusekiServer server = startServer(ds);

        // Example SPARQL query
//        String queryString =
//                "PREFIX musico: <http://purl.org/ontology/musico/> " +
//                        "PREFIX musicoo: <http://purl.org/ontology/musico#> " +
//                        "PREFIX mo: <http://purl.org/ontology/mo/>" +
//                        "PREFIX schema: <https://schema.org/>" +
//                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
//                        "SELECT  * WHERE { " +
//                         " ?s owl:sameAs ?o ." +
//                        "?s a schema:MusicVenue ;" +
//                        "  ?p [a mo:Genre]" +
//                      "?s ?p [a mo:Genre] ." +
//                        "?s a owl:Class ." +
//                        "?class a owl:Class ." +
//                        "[a musico:MusiciansGroup] musico:plays_genre ?genre ." +
//                        "?part musico:involved_event/^owl:sameAs/schema:address ?venue ." +
//                        "?part musico:played_musical_work/mo:genre ?genre ." +
//
//                        "?part musico:involved_event ?event." +
//                        "?event owl:sameAs ?sl." +
//                        "?sl a musico:SelfLearning ." +
//                        "?sl schema:address ?venue ." +
//                        "?part musico:played_musical_work ?work ." +
//                        "?work mo:genre ?genre ." +
//                        "?p ?o [a mo:Genre]." +
//                        "}";


//        execQuery(queryString, baseInf);
        server.start();
        try {
            ServerConn.tryConnection();
        } catch (Exception e) {
            System.out.println(e);
        }
        server.stop();
    }
}
