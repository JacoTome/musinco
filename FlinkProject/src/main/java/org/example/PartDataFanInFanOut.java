package org.example;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class PartDataFanInFanOut {

    static class PartEventFunction implements MapFunction<PartDataRequest, PartData> {
        @Override
        public PartData map(PartDataRequest partDataRequest) {
            final PartData partData = new PartData(partDataRequest.uuid);
            if (partDataRequest.rdfData.getPredicate().contains("involved_event")) {
                partData.setEventID(partDataRequest.getEventID());
            }
            return partData;
        }
    }

    static class PartMoodFunction implements MapFunction<PartDataRequest, PartData> {
        @Override
        public PartData map(PartDataRequest partDataRequest) {
            final PartData partData = new PartData(partDataRequest.uuid);
            if (partDataRequest.rdfData.getPredicate().contains("mood")) {
                partData.setMood(partDataRequest.getMood());
            }
            return partData;
        }
    }

    static class PartDataRequest {
        public final String uuid;
        public RdfData rdfData;

        public PartDataRequest(String uuid, RdfData rdfData) {
            this.uuid = uuid;
            this.rdfData = rdfData;
        }

        public int getEventID() {
            String[] parts = rdfData.getObject().split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        }

        public String getMood() {
            return rdfData.getObject();
        }
    }

    static class FanOut extends ProcessFunction<RdfData, PartData> {
        final static OutputTag<PartDataRequest> moodTag = new OutputTag<>("partMoodRequest") {
        };
        final static OutputTag<PartDataRequest> eventTag = new OutputTag<>("partEventRequest") {
        };

        @Override
        public void processElement(RdfData rdfData, ProcessFunction<RdfData, PartData>.Context context, Collector<PartData> collector) {
            final PartDataRequest req = new PartDataRequest(rdfData.getSubject(), rdfData);
            if (rdfData.getPredicate().contains("involved_event")) {
                context.output(eventTag, req);
            } else if (rdfData.getPredicate().contains("mood")) {
                context.output(moodTag, req);
            }

            final PartData partData = new PartData(rdfData.getSubject());
            partData.setEventID(0);
            partData.setMood(null);
            collector.collect(partData);
        }
    }

    static class PartData {
        public final String uuid;
        private int eventID;
        private String mood;


        PartData(String uuid) {
            this.uuid = uuid;
        }

        public void setEventID(int eventID) {
            this.eventID = eventID;
        }

        public void setMood(String mood) {

            this.mood = mood;
        }

        public String getMood() {
            return mood;
        }
        public int getEventID() {
            return eventID;
        }
        @Override
        public String toString() {
            return "PartData{" +
                    "uuid=" + uuid +
                    ", eventID=" + eventID +
                    ", mood='" + mood + '\'' +
                    '}';
        }

        public PartData combine(final PartData partData) {
            final PartData combined = new PartData(uuid);
            combined.setEventID(eventID != 0 ? eventID : partData.eventID);
            combined.setMood(mood != null ? mood : partData.mood);
            return combined;
        }

        public boolean isComplete() {
            return eventID != 0 && mood != null;
        }
    }

    static class FanIn extends RichFlatMapFunction<PartData, PartData> {
        private transient ValueState<PartData> partDataValueState;

        @Override
        public void flatMap(PartData partData, Collector<PartData> collector) throws Exception {
            PartData current = partDataValueState.value();
            if (current != null) {
                current = current.combine(partData);
            } else {
                current = partData;
            }

            if (current.isComplete()) {
                collector.collect(current);
                partDataValueState.clear();
            } else {
                partDataValueState.update(current);
            }
        }

        @Override
        public void open(Configuration config) {
            ValueStateDescriptor<PartData> descriptor =
                    new ValueStateDescriptor<>(
                            "partData", // the state name
                            PartData.class); // type information
            partDataValueState = getRuntimeContext().getState(descriptor);
        }
    }
}
