package org.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static org.example.SLSDataFIFO.FanOut.dayTag;
import static org.example.SLSDataFIFO.FanOut.hourTag;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

//    private static String getAddress() {
//        String address = "";
//        try {
//            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while (networkInterfaces.hasMoreElements()) {
//                NetworkInterface networkInterface =
//                        networkInterfaces.nextElement();
//                System.out.println(networkInterface.getDisplayName());
//                if (networkInterface.isVirtual()) {
//                    if (networkInterface.getDisplayName().contains("vEthernet")) {
//                        while (networkInterface.getInetAddresses().hasMoreElements()) {
//                            InetAddress tempAddress = networkInterface.getInetAddresses().nextElement();
//                            if (tempAddress.toString().contains("172")) {
//
//                                address = tempAddress + ":9092";
//                                System.out.println(address);
//                            }
//                        }
//                    }
//                }
//            }
//
//        } catch (SocketException e) {
//            throw new RuntimeException(e);
//        }
//        return address;
//    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String address = "172.31.208.1:9092";

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


        DataStream<PartDataFanInFanOut.PartData> joinedStream = PartDataFIFO(env, musicianParticipationSource);
        DataStream<SLSDataFIFO.SLSData> SLSData = SLSDataFIFOStream(env, selfLearningSessionSource);

        SLSData.print();
        joinedStream.print();
        env.execute("RDF Kafka Stream");
    }

    private static DataStream<SLSDataFIFO.SLSData> SLSDataFIFOStream(StreamExecutionEnvironment
                                                                             env, KafkaSource<String> selfLearningSessionSource) {
        DataStream<RdfData> SLSStream = env.fromSource(selfLearningSessionSource,
                WatermarkStrategy.noWatermarks(), "SLSStream").map(RdfData.mapToRdfData);

        SingleOutputStreamOperator<SLSDataFIFO.SLSData> joinStream = SLSStream.process(new SLSDataFIFO.FanOut());
        // Get Side Output for day, hour, minute, second
        DataStream<SLSDataFIFO.SLSData> dateStream = joinStream.getSideOutput(dayTag).map(
                new SLSDataFIFO.SLSDayFunction()
        );
        DataStream<SLSDataFIFO.SLSData> hourStream = joinStream.getSideOutput(hourTag).map(
                new SLSDataFIFO.SLSHourFunction()
        );
        DataStream<SLSDataFIFO.SLSData> minuteStream = joinStream.getSideOutput(SLSDataFIFO.FanOut.minuteTag).map(
                new SLSDataFIFO.SLSMinuteFunction()
        );
        DataStream<SLSDataFIFO.SLSData> secondStream = joinStream.getSideOutput(SLSDataFIFO.FanOut.secondTag).map(
                new SLSDataFIFO.SLSSecondFunction()
        );
        return joinStream
                .union(dateStream, hourStream, minuteStream, secondStream)
                .keyBy((slsData -> slsData.uuid))
                .flatMap(new SLSDataFIFO.FanIn());

    }

    private static DataStream<PartDataFanInFanOut.PartData> PartDataFIFO(StreamExecutionEnvironment
                                                                                 env, KafkaSource<String> musicianParticipationSource) {
        DataStream<RdfData> MPStream = env.fromSource(musicianParticipationSource,
                WatermarkStrategy.noWatermarks(), "MPStream").map(RdfData.mapToRdfData);

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