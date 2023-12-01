package org.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(1000);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        KafkaSource<String> musicianParticipationSource = KafkaSource.<String>builder()
                .setBootstrapServers("172.17.0.1:9092")
                .setTopics("MPStream")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        KafkaSource<String> selfLearningSessionSource = KafkaSource.<String>builder()
                .setBootstrapServers("172.17.0.1:9092")
                .setTopics("SLSStream")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        logger.info("[APP] Starting Kafka Consumer");

        DataStream<String> MPStream = env.fromSource(musicianParticipationSource,
                WatermarkStrategy.noWatermarks(), "MPStream");
        DataStream<String> SLSStream = env.fromSource(selfLearningSessionSource,
                WatermarkStrategy.noWatermarks(), "SLSStream");

        SingleOutputStreamOperator<RdfData> rdfMPStream = MPStream.map(RdfData.mapToRdfData)
                .keyBy(RdfData::getObject)
                .process(new ModelCreationJob())
                .name("Model Creation Job");

        SingleOutputStreamOperator<RdfData> rdfSLSStream = SLSStream.map(RdfData.mapToRdfData)
                .keyBy(RdfData::getObject)
                .process(new ModelCreationJob())
                .name("Model Creation Job");

                }).keyBy(RdfData::getObject)
                .process(new ModelCreationJob())
                .name("Model Creation Job");

        genreStream.keyBy(RdfData::getObject)
                .process(new CountGenre())
                .name("Jena Sink");

        env.execute("RDF Kafka Stream");
    }

}