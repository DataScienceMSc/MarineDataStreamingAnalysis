package team.General;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class Nari_Dynamic_KafkaStream {

    public static void main(String[] args) throws IOException {

        File file = new File("./inputFiles/nari_dynamic_small.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName()); // our key and values are String
        properties.setProperty("value.serializer", StringSerializer.class.getName());
        properties.setProperty("group.id", "group20");

        Producer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(properties);

        String line = " ";

        int key = 0;
        while ((line = br.readLine()) != null) {
            //  System.out.println(line);

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("FinalOpenSeaEntries2", Integer.toString(key), line);
            //System.out.println(Integer.toString(key) + "..." + producerRecord);
            key++;
            producer.send(producerRecord);
            producer.flush();

        }
        System.out.println("exit");

    }
}
