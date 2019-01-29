package team.SimpleEvents;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import team.Executors.SimpleConditionStreamGenerator;

public class FarFromPortsExecutor {


    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStream<String> kafkaStrm = SimpleConditionStreamGenerator.getKafkaStream(env);

        FarFromPorts farFromPorts = new FarFromPorts(kafkaStrm, "./results/FarFromPorts.csv",env);
        Thread t=new Thread(farFromPorts);
        t.start();

    }
}