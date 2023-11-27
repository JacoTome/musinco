package com.mycompany.app;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.system.Txn;

public class ServerConn {

    public static final String PREFIXES = "PREFIX musinco:  <http://www.semanticweb.org/jaco/ontologies/2023/7/musinco#> \n " +
            "PREFIX schema: <https://schema.org/> \n " +
            "PREFIX musico: <http://purl.org/ontology/musico/> \n" +
            "PREFIX time:  <http://www.w3.org/2006/time#> \n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
            "PREFIX mo: <http://purl.org/ontology/mo/> \n " +
            "PREFIX musicoo: <http://purl.org/ontology/musico#> \n ";

    public static void tryInsertQuery(RDFConnection conn) {
        System.out.println("[APP] -- Started Insert Query");
        long startTime = System.nanoTime();

        Txn.executeWrite(conn, () -> {
            conn.update(PREFIXES +
                    "INSERT DATA{ \n " +
                    "<musinco:JacoArtist> musicoo:plays_genre <http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Genre/809> ;\n " +
                    " musico:hasGroup <musinco:JacoGroup> .\n " +
                    "}\n");
            System.out.println("Inserted");
        });

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time: " + duration / 1000000 + "ms");
        System.out.println("[APP] -- Ended Insert Query");

    }

    public static void trySelectQuery(RDFConnection conn) {
        System.out.println("[APP] -- Started Select Query");
        long startTime = System.nanoTime();

        conn.querySelect(PREFIXES +
                "SELECT DISTINCT * WHERE { " +
                " ?s musico:displayed_mood ?o  " +
                "} " +
                "LIMIT 50", System.out::println);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time: " + duration / 1000000 + "ms");
        System.out.println("[APP] -- Ended Select Query");

    }

    public static void tryConnection() {
        // Measure time
        try (RDFConnection conn = RDFConnection.connect("http://localhost:3030/ds")) {
//            tryInsertQuery(conn);
            trySelectQuery(conn);


        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void main(String[] args) {
        try (RDFConnection conn = RDFConnection.connect("http://localhost:3030/ds")) {

            conn.querySelect("SELECT DISTINCT ?s { ?s ?p ?o } LIMIT 10", (qs) -> {
                Resource subject = qs.getResource("s");
                System.out.println("Subject: " + subject);
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
