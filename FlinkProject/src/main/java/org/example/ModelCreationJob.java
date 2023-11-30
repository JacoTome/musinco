package org.example;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

public class ModelCreationJob extends ProcessFunction<RdfData, RdfData> {



    @Override
    public void processElement(RdfData rdfData, Context context, Collector<RdfData> collector) throws Exception {
        String subject = rdfData.getSubject();
        String predicate = rdfData.getPredicate();
        String object = rdfData.getObject();

        System.out.println("---------------------");
        System.out.println("Subject: " + subject);
        System.out.println("Predicate: " + predicate);
        System.out.println("Object: " + object);
    }
}
