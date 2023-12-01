package org.example;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class ModelCreationJob extends KeyedProcessFunction<String,RdfData, RdfData> {

    private static final String MUSINCO_PREFIX = "http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/";
    public transient ValueState<Integer> count;

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        ValueStateDescriptor<Integer> descriptor = new ValueStateDescriptor<Integer>("count", Integer.class);
        count = getRuntimeContext().getState(descriptor);
    }
    @Override
    public void processElement(RdfData rdfData, Context context, Collector<RdfData> collector) throws Exception {
        String subject = rdfData.getSubject();
        String predicate = rdfData.getPredicate();
        String object = rdfData.getObject();

        System.out.println("Subject: " + subject);
        System.out.println("Predicate: " + predicate);
        System.out.println("Object: " + object);
        collector.collect(rdfData);
    }
}
