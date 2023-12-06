package org.example;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.eclipse.rdf4j.model.base.CoreDatatype;

import java.io.Serializable;

public class RdfData implements Serializable {

    public RdfData(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    private String subject;
    private String predicate;
    private String object;

    public static Integer getCount() {
        return count;
    }

    public static void setCount(Integer count) {
        RdfData.count = count;
    }

    private static Integer count = 0;

    public static MapFunction<String, RdfData> mapToRdfData = new MapFunction<String, RdfData>() {
        @Override
        public RdfData map(String s) throws Exception {
            s = s.replace("(", "");
            s = s.replace(")", "");
            s = s.replace(",", "");
            String[] parts = s.split(" ");
            return new RdfData(parts[0], parts[1], parts[2]);
        }
    };

    @Override
    public String toString() {
        return "RdfData{" +
                "\nsubject='" + subject + '\'' +
                ", \npredicate='" + predicate + '\'' +
                ", \nobject='" + object + '\'' +
                '}';
    }
}
