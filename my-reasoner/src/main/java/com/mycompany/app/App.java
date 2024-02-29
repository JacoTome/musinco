package com.mycompany.app;

import org.apache.commons.io.IOUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.TDB2Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.jena.vocabulary.ReasonerVocabulary.PROPderivationLogging;
import static org.apache.jena.vocabulary.ReasonerVocabulary.PROPtraceOn;

public class App {
    public static final String MUSINCO = "src/main/resources/musinco5.rdf";
    public static final String RULES = "src/main/resources/myrules.ttl";
    public static final String DATA = "src/main/resources/musinco4-materialized.xml";

    public static FusekiServer startServer(Dataset ds) {
        FusekiLogging.setLogging();
        return FusekiServer.create()
                .verbose(true)
                .add("/ds", ds)
                .port(3030)
                .build();
    }

    private InputStream getResourceAsStream(String s) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(s);
        if (in == null) {
            throw new IllegalArgumentException("File not found: " + s);
        }
        return in;
    }

    public static void main(String[] args) throws IOException {


        // Load ontology


        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.read(MUSINCO, "RDF/XML");
//        ontModel.read(DATA, "RDF/XML");
        OntModel ontModelInf = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, ontModel);
        System.out.println("Ontology loaded");

        Model baseInf = ontModelInf.getBaseModel();
        System.out.println("Base model created");


        // Create a reasoner
        GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL(RULES));
        reasoner.setParameter(PROPtraceOn, true);
        reasoner.setParameter(PROPderivationLogging, true);
        System.out.println("Reasoner created");

        // Create an inference model
        InfModel inf = ModelFactory.createInfModel(reasoner, baseInf);
        System.out.println("Inference model created");

        // create Dataset from inf model
        Dataset ds = TDBFactory.createDataset("./database/musincodb");
        ds.begin(ReadWrite.WRITE);
        Model data = ModelFactory.createDefaultModel();
        data.read(DATA);
        Model model = ds.getDefaultModel();
        model.add(data);
        model.add(inf);
        ds.commit();
        ds.end();

        System.out.println("Dataset created");
        FusekiServer server = startServer(ds);


//       execQuery(queryString, baseInf);
        server.start();

    }
}
