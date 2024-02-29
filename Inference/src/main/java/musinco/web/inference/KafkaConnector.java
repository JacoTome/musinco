package musinco.web.inference;



import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class KafkaConnector {

    private class Statement{
        String subject;
        String predicate;
        String object;

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

        @Override
        public String toString() {
            return
                    "(?s=" + subject + ") " +
                    "(?p=" + predicate + ") " +
                    "(?o=" + object + ") \n" ;

        }
    }
    public KafkaConnector() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(properties);
    }

    private static KafkaProducer<String, String> producer = null;
    private static final String bootstrapServers = "localhost:9092";
    private static final String jena_fuseki_url = "http://204.216.223.231:3030/musincoWebApp/";

    private static RDFConnection createConnection() {
        return RDFConnectionRemote.newBuilder()
                .destination(jena_fuseki_url)
                .queryEndpoint("sparql")
                .updateEndpoint("update")
                .acceptHeaderSelectQuery("application/xml")
                .build();
    }

    static String execTestSparqlQuery() {
        StringBuilder result = new StringBuilder();
        try (RDFConnection conn = createConnection()) {
            String query = "SELECT * WHERE {?s ?p ?o} LIMIT 10";
            conn.querySelect(query, querySolution -> {
                result.append(querySolution.get("s").toString()).append(" \n ");
                result.append(querySolution.get("p").toString()).append(" \n ");
                result.append(querySolution.get("o").toString()).append(". \n ");
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private List<Statement>  execMPSparqlQuery() {
        String query = "PREFIX musicoo: <http://purl.org/ontology/musico/> \n" +
                "SELECT * WHERE {?s ?p ?o;" +
                "a musicoo:MusicianParticipation} limit 10 ";
       List<Statement> result = new ArrayList<>();
        try (RDFConnection conn = createConnection()) {

            conn.querySelect(query, querySolution -> {
                Statement st = new Statement();
                st.setSubject(querySolution.get("s").toString());
                st.setPredicate(querySolution.get("p").toString());
                st.setObject(querySolution.get("o").toString());
                result.add(st);

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<Statement> execSLSSparqlQuery() {
        String query = "PREFIX musicoo: <http://purl.org/ontology/musico/> \n" +
                "SELECT * WHERE {?s ?p ?o;" +
                "a musicoo:SelfLearning} limit 10 ";
        List<Statement> result = new ArrayList<>();
        try (RDFConnection conn = createConnection()){
            conn.querySelect(query, querySolution -> {
                Statement st = new Statement();
                st.setSubject(querySolution.get("s").toString());
                st.setPredicate(querySolution.get("p").toString());
                st.setObject(querySolution.get("o").toString());
                result.add(st);

            });

        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    void  sendDataForInference() {
        List<Statement> result = new ArrayList<>(execMPSparqlQuery());
        result.addAll(execSLSSparqlQuery());
        for (Statement statement : result) {
            String subject = statement.getSubject();
            if (subject.contains("MusicianParticipation") ){
                ProducerRecord<String, String> record = new ProducerRecord<>("MPStream", statement.toString());
                System.out.println(record);
                producer.send(record);
                producer.flush();
            }
            if (subject.contains("SelfLearningSession")) {
                ProducerRecord<String, String> record = new ProducerRecord<>("SLSStream", statement.toString());
                producer.send(record);
                producer.flush();
            }
        }
        producer.close();

    }
}



//
//
//    public static void main(String[] args) throws IOException {
//
//
//        String bootstrapServers = "localhost" +
//                ":9092";
//
//        // create producer
//        Properties properties = new Properties();
//        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//
//        Model model = new LinkedHashModel();
//
//        rdfParser.setRDFHandler(new StatementCollector(model));
//
//        // Load xml file
//        String fileName = "src/main/resources/musinco4-materialized.xml";
//        File file = new File(fileName);
//        InputStream inputStream = new FileInputStream(file);
//
//        try {
//            rdfParser.parse(inputStream, "");
//        } catch (Exception e) {
//            System.out.println("Error parsing file");
//        }
//
//        // create the producer
//        logger.info("Creating producer");
//        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
//
//        logger.info("Producer created");
//        // create a producer record
//
//        for (Statement statement : model) {
//            String subject = statement.getSubject().toString();
//            if (subject.contains("MusicianParticipation") ){
//                ProducerRecord<String, String> record = new ProducerRecord<>("MPStream", statement.toString());
//                producer.send(record);
//                producer.flush();
//            }
//            if (subject.contains("SelfLearningSession")) {
//                ProducerRecord<String, String> record = new ProducerRecord<>("SLSStream", statement.toString());
//                producer.send(record);
//                producer.flush();
//            }
//        }
//        producer.close();
//    }
