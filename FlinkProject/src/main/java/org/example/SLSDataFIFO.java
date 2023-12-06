package org.example;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class SLSDataFIFO {

    static class SLSDayFunction implements MapFunction<SLSDataRequest, SLSData> {
        @Override
        public SLSData map(SLSDataRequest slsDataRequest) throws Exception {
            final SLSData slsData = new SLSData(slsDataRequest.uuid);
            slsData.setDate(slsDataRequest.rdfData.getObject());
            return slsData;
        }
    }

    static class SLSHourFunction implements MapFunction<SLSDataRequest, SLSData> {
        @Override
        public SLSData map(SLSDataRequest slsDataRequest) throws Exception {
            //Example of object data object='"6"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger>'
            final SLSData slsData = new SLSData(slsDataRequest.uuid);
            String[] parts = slsDataRequest.rdfData.getObject().split("\"");
            System.out.println("Parsed Hour: " +parts[1]);
            slsData.setHour(Integer.parseInt(parts[1]));
            return slsData;
        }
    }

    static class SLSMinuteFunction implements MapFunction<SLSDataRequest, SLSData> {
        @Override
        public SLSData map(SLSDataRequest slsDataRequest) throws Exception {
            //Example of object data object='"6"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger>'
            final SLSData slsData = new SLSData(slsDataRequest.uuid);
            String[] parts = slsDataRequest.rdfData.getObject().split("\"");
            slsData.setMinute(Integer.parseInt(parts[1]));
            return slsData;
        }
    }

    static class SLSSecondFunction implements MapFunction<SLSDataRequest, SLSData> {
        @Override
        public SLSData map(SLSDataRequest slsDataRequest) throws Exception {
            //Example of object data object='"6"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger>'
            final SLSData slsData = new SLSData(slsDataRequest.uuid);
            String[] parts = slsDataRequest.rdfData.getObject().split("\"");
            slsData.setSecond(Integer.parseInt(parts[1]));
            return slsData;
        }
    }
    static class SLSDataRequest {
        public final String uuid;
        public RdfData rdfData;

        public SLSDataRequest(String uuid, RdfData rdfData) {
            this.uuid = uuid;
            this.rdfData = rdfData;
        }
    }
    static class SLSData {

        public final String uuid;

        public void setEventID(int eventID) {
            this.eventID = eventID;
        }

        private String date;
        private int hour;
        private int minute;
        private int second;
        private int eventID;

        public SLSData(String uuid) {
            this.uuid = uuid;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public boolean isComplete() {
            return date != null && hour != 0 && minute != 0 && second != 0;
        }

        public SLSData combine(SLSData other) {
            final SLSData slsDataCombined = new SLSData(this.uuid);
            slsDataCombined.setSecond(this.second == -1 ? other.second : this.second);
            slsDataCombined.setMinute(this.minute == -1 ? other.minute : this.minute);
            slsDataCombined.setHour(this.hour == -1 ? other.hour : this.hour);
            slsDataCombined.setDate(this.date == null ? other.date : this.date);
            return slsDataCombined;
        }
        @Override
        public String toString() {
            return "SLSData{" +
                    "\nuuid='" + uuid + '\'' +
                    ", \ndate='" + date + '\'' +
                    ", \nhour=" + hour +
                    ", \nminute=" + minute +
                    ", \nsecond=" + second +
                    ", \neventID=" + eventID +
                    '}';
        }
    }

    static class FanOut extends ProcessFunction<RdfData, SLSData> {
        // Tag for day, hour, minute, second
        final static OutputTag<SLSDataRequest> dayTag = new OutputTag<>("dayData") {

        };
        final static OutputTag<SLSDataRequest> hourTag = new OutputTag<>("hourData") {

        };
        final static OutputTag<SLSDataRequest> minuteTag = new OutputTag<>("minuteData") {

        };
        final static OutputTag<SLSDataRequest> secondTag = new OutputTag<>("secondData") {

        };

        final static OutputTag<SLSDataRequest> eventTag = new OutputTag<>("eventData") {

        };

        @Override
        public void processElement(RdfData rdfData, ProcessFunction<RdfData, SLSData>.Context context, Collector<SLSData> collector) throws Exception {
            final SLSDataRequest req = new SLSDataRequest(rdfData.getSubject(), rdfData);
            if (rdfData.getPredicate().contains("day")) {
                context.output(dayTag, req);
            } else if (rdfData.getPredicate().contains("hour")) {
                context.output(hourTag, req);
            } else if (rdfData.getPredicate().contains("minute")) {
                context.output(minuteTag, req);
            } else if (rdfData.getPredicate().contains("second")) {
                context.output(secondTag, req);
            }

            final SLSData slsData = new SLSData(req.uuid);
            slsData.date = null;
            slsData.hour = -1;
            slsData.minute = -1;
            slsData.second = -1;
            slsData.eventID = 0;

            collector.collect(slsData);
        }
    }

    static class FanIn extends RichFlatMapFunction<SLSData, SLSData> {
        private transient ValueState<SLSData> SLSDataState;
        @Override
        public void flatMap(SLSData slsData, Collector<SLSData> collector) throws Exception {
            SLSData currentSLSData = SLSDataState.value();
            if (currentSLSData != null) {
                currentSLSData = currentSLSData.combine(slsData);
            } else {
                currentSLSData = slsData;
            }

            if (currentSLSData.isComplete()) {
                collector.collect(currentSLSData);
                SLSDataState.clear();
            } else {
                SLSDataState.update(currentSLSData);
            }
        }
        @Override
        public void open(Configuration config) {
            ValueStateDescriptor<SLSData> descriptor =
                    new ValueStateDescriptor<>(
                            "SLSData", // the state name
                            SLSData.class); // type information
            SLSDataState = getRuntimeContext().getState(descriptor);
        }
    }
}
