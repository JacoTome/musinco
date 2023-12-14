package org.example;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.BufferedWriter;
import java.io.File;

public class JenaSink implements SinkFunction<RdfData> {



        @Override
        public void invoke(RdfData value, Context context) throws Exception {
                // Push to jena database



        }

}
