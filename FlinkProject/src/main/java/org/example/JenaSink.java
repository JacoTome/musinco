package org.example;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.lang.SPARQLParserFactory;
import org.apache.jena.sparql.lang.sparql_11.SPARQLParser11;
import org.apache.jena.system.Txn;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.List;

public class JenaSink implements SinkFunction<List<RdfData>> {
    // Insert into Jena Fuseki Database
    private static final String musincoPrefix = "http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/";
    private static final String musincoPrefixShort = "musinco:";
    private static final String databaseAddress = System.getenv("JENA_FUSEKI_URL");

     static RDFConnection createConnection() {

        return RDFConnectionRemote.newBuilder()
                .destination("http://204.216.223.231:3030/musincoWebApp")
                .queryEndpoint("sparql")
                .updateEndpoint("update")
                .acceptHeaderAskQuery("application/sparql-results+json")
                .build();
    }

    @Override
    public void invoke(List<RdfData> rdfDataList, Context context) throws Exception {
        StringBuilder query = new StringBuilder("INSERT DATA {\n");
        for (RdfData rdf : rdfDataList) {
            query.append("<").append(rdf.getSubject()).append("> <").append(rdf.getPredicate()).append("> <").append(rdf.getObject()).append("> .\n");
        }
        query.append("}");

        // DecodeURI to get the correct URI
        try (RDFConnection conn = createConnection()) {
            System.out.println(query.toString());
           conn.update(query.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testConnection() {
        try (RDFConnection conn = createConnection()) {
            String query = " INSERT DATA{ <http://example/book3> <http://example.org/ns#price> 42 }";
            conn.update(query);
        }
    }

}
