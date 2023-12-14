package org.example;

import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.api.common.functions.RichFlatJoinFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.List;

public class EventData {

    private static final HashMap<Integer, EventDataObject> eventDataObjectHashMap = new HashMap<>();
    static class EventDataObject {
        public final int uuid; // Event ID
        public SLSDataFIFO.SLSData slsData;
        public List<PartDataFanInFanOut.PartData> partDataList = new java.util.ArrayList<>();

        public EventDataObject(int uuid) {
            this.uuid = uuid;
        }


        @Override
        public String toString() {
            return "EventDataObject{" +
                    "\nuuid=" + uuid +
                    ",\nslsData=" + slsData +
                    ",\npartDataList=" + partDataList.toString() +
                    '}';
        }
    }

    static class CoMapEventData extends RichCoFlatMapFunction<SLSDataFIFO.SLSData, PartDataFanInFanOut.PartData, EventDataObject> {

        @Override
        public void flatMap1(SLSDataFIFO.SLSData slsData, Collector<EventDataObject> collector) throws Exception {
            // Retrieve EventDataObject from HashMap
            EventDataObject eventDataObject = eventDataObjectHashMap.get(slsData.getEventID());
            if (eventDataObject == null) {
                eventDataObject = new EventDataObject(slsData.getEventID());
                eventDataObject.slsData = slsData;
                eventDataObjectHashMap.put(slsData.getEventID(), eventDataObject);
            } else {
                eventDataObject.slsData = (eventDataObject.slsData == null ? slsData : null);
                collector.collect(eventDataObject);
            }
        }

        @Override
        public void flatMap2(PartDataFanInFanOut.PartData partData, Collector<EventDataObject> collector) throws Exception {
            // Retrieve EventDataObject from HashMap
            EventDataObject eventDataObject = eventDataObjectHashMap.get(partData.getEventID());
            if (eventDataObject == null) {
                eventDataObject = new EventDataObject(partData.getEventID());
                eventDataObject.partDataList.add(partData);
                eventDataObjectHashMap.put(partData.getEventID(), eventDataObject);
            } else {
                if (!eventDataObject.partDataList.contains(partData)) {
                    eventDataObject.partDataList.add(partData);
                }
                if (eventDataObject.slsData != null && eventDataObject.partDataList.size() > 1) {
                    collector.collect(eventDataObject);
                    eventDataObjectHashMap.remove(partData.getEventID());
                }
            }
        }

    }

    static class EventFlatJoinFunction extends RichFlatJoinFunction<PartDataFanInFanOut.PartData, SLSDataFIFO.SLSData, EventDataObject> {

        private transient ValueState<EventDataObject> eventDataObjectValueState;

        @Override
        public void join(PartDataFanInFanOut.PartData partData, SLSDataFIFO.SLSData slsData, Collector<EventDataObject> collector) throws Exception {
            EventDataObject eventDataObject = eventDataObjectValueState.value();
            if (eventDataObject == null) {
                eventDataObject = new EventDataObject(partData.getEventID());
                eventDataObject.slsData = slsData;
                eventDataObject.partDataList.add(partData);
            } else {
                eventDataObject.slsData = (eventDataObject.slsData == null ? slsData : null);
                if (!eventDataObject.partDataList.contains(partData)) {
                    eventDataObject.partDataList.add(partData);
                }
            }

            System.out.println(eventDataObject);
            if (eventDataObject.partDataList.size() < 2) {
                eventDataObjectValueState.update(eventDataObject);
            } else {
                collector.collect(eventDataObject);
                eventDataObjectValueState.clear();
            }
        }

        @Override
        public void open(Configuration config) {
            ValueStateDescriptor<EventDataObject> desc =
                    new ValueStateDescriptor<>("EventData", EventDataObject.class);
            eventDataObjectValueState = getRuntimeContext().getState(desc);
        }
    }
}
