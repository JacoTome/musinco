package com.mycompany.app;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;

public class ServerConn {
    public static void tryConnection() {
        // Measure time
        long startTime = System.nanoTime();
        try (RDFConnection conn = RDFConnection.connect("http://localhost:3030/ds")) {
            conn.querySelect("SELECT DISTINCT * { ?s ?p ?o } LIMIT 10", (qs) -> {
                Resource subject = qs.getResource("p");
                System.out.println("Predicate: " + subject);
            });
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("Time: " + duration / 1000000 + "ms");

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
