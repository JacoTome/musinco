package org.example;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;


public class CountGenre extends KeyedProcessFunction<String, RdfData, RdfData> {

    @Override
    public void processElement(RdfData rdfData, KeyedProcessFunction<String, RdfData, RdfData>.Context context, Collector<RdfData> collector) throws Exception {

        int count = RdfData.getCount();
        if (count > 20) {
            System.out.println("Count is " + count);
            RdfData.setCount(0);
            collector.collect(rdfData);
        }
    }
}
