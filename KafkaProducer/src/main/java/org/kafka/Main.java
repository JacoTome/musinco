package org.kafka;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.UnknownHostException;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);


    public static void main(String[] args) throws IOException{


        String bootstrapServers = "192.168.0.6:9092";

        // create producer
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Model model = new LinkedHashModel();

        rdfParser.setRDFHandler(new StatementCollector(model));

        // Load xml file
        String fileName = "src/main/resources/musinco2-materialized.xml";
        File file = new File(fileName);
        InputStream inputStream = new FileInputStream(file);

        try {
            rdfParser.parse(inputStream, "");
        } catch (Exception e) {
            System.out.println("Error parsing file");
        }

        // create the producer
        logger.info("Creating producer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        logger.info("Producer created");
        // create a producer record

        for (Statement statement : model) {
            String subject = statement.getSubject().toString();
//                if (subject.contains("MusicianParticipation")) {
                    logger.info("Sending MP statement");
                    ProducerRecord<String, String> record = new ProducerRecord<>("MPStream", statement.toString());
                    producer.send(record);
                    producer.flush();
//                }
        }
        producer.close();
    }
}