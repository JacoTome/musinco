package org.example;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.protocol.types.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CountMoodJob {
    // Aggregate function to count the number of times each mood appears every hour
    public static class AggregateMoodFunction
            implements AggregateFunction<EventData.EventDataObject, HashMap<Integer,HashMap<String, Integer>>, List<RdfData>> {

        @Override
        public HashMap<Integer,HashMap<String, Integer>> createAccumulator() {
            return new HashMap<Integer, HashMap<String, Integer>>() {
            };
        }

        @Override
        public HashMap<Integer,HashMap<String, Integer>> add(
                EventData.EventDataObject eventDataObject,
                HashMap<Integer,HashMap<String, Integer>> HourMoodCount) {
            int hour = eventDataObject.slsData.getHour();
            for (PartDataFanInFanOut.PartData partData : eventDataObject.partDataList) {
                String mood = partData.getMood();
                if (HourMoodCount.get(hour) == null) {
                    HourMoodCount.put(hour, new HashMap<String, Integer>());
                } else {
                    if (HourMoodCount.get(hour).containsKey(mood)) {
                        HourMoodCount.get(hour).put(mood, HourMoodCount.get(hour).get(mood) + 1);
                    } else {
                        HourMoodCount.get(hour).put(mood, 1);
                    }
                }
            }
            return HourMoodCount;
        }

        @Override
        public List<RdfData> getResult(HashMap<Integer,HashMap<String, Integer>> HourMoodCount) {
            final String musincoPrefix = "http://www.semanticweb.org/jaco/ontologies/2023/7/musinco";
            List<RdfData> result = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                // Get max value for each hour
                int max = 0;

                String maxMood = "";
                if (HourMoodCount.get(i) == null) {
                    continue;
                }
                // Find max value of count in this hour
                for (String mood : HourMoodCount.get(i).keySet()) {
                    if (HourMoodCount.get(i).get(mood) > max) {
                        max = HourMoodCount.get(i).get(mood);
                    }
                }

                // Find all moods with max value
                for (String mood: HourMoodCount.get(i).keySet()) {
                    if (HourMoodCount.get(i).get(mood) == max) {

                        RdfData rdfData = new RdfData(
                                musincoPrefix + "/hour_" + i,
                                musincoPrefix + "#popular_mood",
                                 mood
                        );
                        RdfData hourData = new RdfData(
                                musincoPrefix + "/hour_" + i,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",//rdf type
                                "https://schema.org/Time"
                        );
                        result.add(rdfData);
                        result.add(hourData);
                    }
                }
                HourMoodCount.get(i).clear();
            }
            return result;
        }

        @Override
        public HashMap<Integer,HashMap<String, Integer>> merge(
                HashMap<Integer,HashMap<String, Integer>> hashMaps,
                HashMap<Integer, HashMap<String, Integer>> acc1) {
            for (int i = 0; i < 24; i++) {
                for (String mood : hashMaps.get(i).keySet()) {
                    if (acc1.get(i).containsKey(mood)) {
                        acc1.get(i).put(mood, acc1.get(i).get(mood) + hashMaps.get(i).get(mood));
                    } else {
                        acc1.get(i).put(mood, hashMaps.get(i).get(mood));
                    }
                }
            }
            return acc1;
        }
    }
}
