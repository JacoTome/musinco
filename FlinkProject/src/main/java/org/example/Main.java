package org.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.CloseableIterable;
import org.apache.flink.util.CloseableIterator;
import org.apache.flink.util.Collector;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String address = System.getenv("KAFKA_ADDRESS");

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        KafkaSource<String> musicianParticipationSource = KafkaSource.<String>builder()
                .setBootstrapServers(" 172.30.176.1:9092")
                .setTopics("MPStream")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        KafkaSource<String> selfLearningSessionSource = KafkaSource.<String>builder()
                .setBootstrapServers(" 172.30.176.1:9092")
                .setTopics("SLSStream")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        logger.info("[APP] Starting Kafka Consumer");
//
//        DataStream<RdfData> MPStream = env.fromSource(musicianParticipationSource,
//                WatermarkStrategy.forMonotonousTimestamps(), "MPStream").map(RdfData.mapToRdfData);
//
//        DataStream<RdfData> SLSStream = env.fromSource(selfLearningSessionSource, WatermarkStrategy.forMonotonousTimestamps()
//                , "SLSStream").map(RdfData.mapToRdfData);
//        // Create a rdfData list

//        SingleOutputStreamOperator<Object> query = MPStream.connect(SLSStream).map(new CoMapFunction<RdfData, RdfData, Object>() {
//
//            @Override
//            public Object map1(RdfData rdfData) throws Exception {
//                StringBuilder query = new StringBuilder();
//                    if(
//                            rdfData.getObject().isEmpty()
//                                    || rdfData.getPredicate().isEmpty()
//                                    || rdfData.getSubject().isEmpty()
//                    ){
//                        return null;
//                    }
//                    query.append("<").append(rdfData.getSubject()).append("> <").append(rdfData.getPredicate()).append("> <").append(rdfData.getObject()).append("> .\n");
//
//
//                return query;
//            }
//
//            @Override
//            public Object map2(RdfData rdfData) throws Exception {
//                StringBuilder query = new StringBuilder();
//
//                if(
//                        rdfData.getObject().isEmpty()
//                        || rdfData.getPredicate().isEmpty()
//                        || rdfData.getSubject().isEmpty()
//                ){
//                    return null;
//                }
//
//                    query.append("<").append(rdfData.getSubject()).append("> <").append(rdfData.getPredicate()).append("> <").append(rdfData.getObject()).append("> .\n");
//
//
//
//                return query;
//            }
//
//        }).windowAll(ProcessingTimeSessionWindows.withGap(Time.seconds(5)))
//                .apply(new AllWindowFunction<Object, Object, TimeWindow>() {
//                    @Override
//                    public void apply(TimeWindow window, Iterable<Object> values, Collector<Object> out) throws Exception {
//                        StringBuilder query = new StringBuilder("INSERT DATA {\n");
//                        for (Object value : values) {
//                            query.append(value);
//                        }
//                        query.append("}");
//                        out.collect(query);
//                    }
//                });
//
//
//        query.addSink(new SinkFunction<Object>() {
//            @Override
//            public void invoke(Object data, Context context) throws Exception {
//
//                try (RDFConnection conn = JenaSink.createConnection()) {
//                    conn.update(data.toString());
//                    System.out.println("Data inserted");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });



        DataStream<PartDataFanInFanOut.PartData> PartDataStream = PartDataFIFO(env, musicianParticipationSource);
        DataStream<SLSDataFIFO.SLSData> SLSData = SLSDataFIFOStream(env, selfLearningSessionSource);

        // Merge streams and key by eventID

        DataStream<PartDataFanInFanOut.PartData> PartDataStreamKeyed = PartDataStream.keyBy((partData -> partData.uuid));
        DataStream<SLSDataFIFO.SLSData> SLSDataStreamKeyed = SLSData.keyBy((slsData -> slsData.uuid));

        SingleOutputStreamOperator<EventData.EventDataObject> eventDataStream =
                SLSDataStreamKeyed.connect(PartDataStreamKeyed)
                .flatMap(new EventData.CoMapEventData())
                ;

        eventDataStream
                .windowAll(ProcessingTimeSessionWindows.withGap(Time.seconds(30)))
                .aggregate(new CountMoodJob.AggregateMoodFunction())
                        .addSink(new JenaSink());
        env.execute("RDF Kafka Stream");
    }

    private static DataStream<SLSDataFIFO.SLSData> SLSDataFIFOStream(StreamExecutionEnvironment
                                                                             env, KafkaSource<String> selfLearningSessionSource) {
        DataStream<RdfData> SLSStream = env.fromSource(selfLearningSessionSource, WatermarkStrategy.forMonotonousTimestamps()
                , "SLSStream").map(RdfData.mapToRdfData);

        SingleOutputStreamOperator<SLSDataFIFO.SLSData> joinStream = SLSStream.process(new SLSDataFIFO.FanOut());
        // Get Side Output for day, hour, minute, second
        DataStream<SLSDataFIFO.SLSData> dateStream = joinStream.getSideOutput(SLSDataFIFO.FanOut.dayTag).map(
                new SLSDataFIFO.SLSDayFunction()
        );
        DataStream<SLSDataFIFO.SLSData> hourStream = joinStream.getSideOutput(SLSDataFIFO.FanOut.hourTag).map(
                new SLSDataFIFO.SLSHourFunction()
        );
        DataStream<SLSDataFIFO.SLSData> minuteStream = joinStream.getSideOutput(SLSDataFIFO.FanOut.minuteTag).map(
                new SLSDataFIFO.SLSMinuteFunction()
        );
        DataStream<SLSDataFIFO.SLSData> secondStream = joinStream.getSideOutput(SLSDataFIFO.FanOut.secondTag).map(
                new SLSDataFIFO.SLSSecondFunction()
        );
        DataStream<SLSDataFIFO.SLSData> eventStream = joinStream.getSideOutput(SLSDataFIFO.FanOut.eventTag).map(
                new SLSDataFIFO.SLSEventFunction()
        );

        return joinStream
                .union(dateStream, hourStream, minuteStream, secondStream, eventStream)
                .keyBy((slsData -> slsData.uuid))
                .flatMap(new SLSDataFIFO.FanIn());

    }

    private static DataStream<PartDataFanInFanOut.PartData> PartDataFIFO(StreamExecutionEnvironment
                                                                                 env, KafkaSource<String> musicianParticipationSource) {
        DataStream<RdfData> MPStream = env.fromSource(musicianParticipationSource,
                WatermarkStrategy.forMonotonousTimestamps(), "MPStream").map(RdfData.mapToRdfData);

        SingleOutputStreamOperator<PartDataFanInFanOut.PartData> joinStream = MPStream.process(new PartDataFanInFanOut.FanOut());

        DataStream<PartDataFanInFanOut.PartData> eventStream = joinStream.getSideOutput(PartDataFanInFanOut.FanOut.eventTag).map(
                new PartDataFanInFanOut.PartEventFunction()
        );
        DataStream<PartDataFanInFanOut.PartData> moodStream = joinStream.getSideOutput(PartDataFanInFanOut.FanOut.moodTag).map(
                new PartDataFanInFanOut.PartMoodFunction()
        );
        return joinStream
                .union(eventStream, moodStream)
                .keyBy((partData -> partData.uuid))
                .flatMap(new PartDataFanInFanOut.FanIn());
    }

}