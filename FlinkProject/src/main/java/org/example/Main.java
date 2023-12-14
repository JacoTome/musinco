package org.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        String address = "172.17.0.1:9093";

        KafkaSource<String> musicianParticipationSource = KafkaSource.<String>builder()
                .setBootstrapServers(address)
                .setTopics("MPStream")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        KafkaSource<String> selfLearningSessionSource = KafkaSource.<String>builder()
                .setBootstrapServers(address)
                .setTopics("SLSStream")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        logger.info("[APP] Starting Kafka Consumer");


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
                .aggregate(new CountMoodJob.AggregateMoodFunction()).print();
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