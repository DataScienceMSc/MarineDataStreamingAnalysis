package team.Executors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import team.SimpleEvents.*;



public class SimplePatternExecutor {


    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env1 = StreamExecutionEnvironment.getExecutionEnvironment();
        env1.setParallelism(1);
        StreamExecutionEnvironment env2 = StreamExecutionEnvironment.getExecutionEnvironment();
        env2.setParallelism(1);
        StreamExecutionEnvironment env3 = StreamExecutionEnvironment.getExecutionEnvironment();
        env3.setParallelism(1);
        StreamExecutionEnvironment env4 = StreamExecutionEnvironment.getExecutionEnvironment();
        env4.setParallelism(1);
        StreamExecutionEnvironment env5 = StreamExecutionEnvironment.getExecutionEnvironment();
        env5.setParallelism(1);
        StreamExecutionEnvironment env6 = StreamExecutionEnvironment.getExecutionEnvironment();
        env6.setParallelism(1);
        StreamExecutionEnvironment env7 = StreamExecutionEnvironment.getExecutionEnvironment();
        env7.setParallelism(1);
        StreamExecutionEnvironment env8 = StreamExecutionEnvironment.getExecutionEnvironment();
        env8.setParallelism(1);

        DataStream<String> kafkaStrm1 = SimpleConditionStreamGenerator.getKafkaStream(env1);
        DataStream<String> kafkaStrm2 = SimpleConditionStreamGenerator.getKafkaStream(env2);
        DataStream<String> kafkaStrm3 = SimpleConditionStreamGenerator.getKafkaStream(env3);
        DataStream<String> kafkaStrm4 = SimpleConditionStreamGenerator.getKafkaStream(env4);
        DataStream<String> kafkaStrm5 = SimpleConditionStreamGenerator.getKafkaStream(env5);
        DataStream<String> kafkaStrm6 = SimpleConditionStreamGenerator.getKafkaStream(env6);
        DataStream<String> kafkaStrm7 = SimpleConditionStreamGenerator.getKafkaStream(env7);
        DataStream<String> kafkaStrm8 = SimpleConditionStreamGenerator.getKafkaStream(env8);



        Acceleration acceleration = new Acceleration(kafkaStrm1,"./results/Acceleration.csv",env1);
        Thread t1=new Thread(acceleration);
        t1.start();

        SpeedChange speedChange = new SpeedChange(kafkaStrm2, "./results/speedChange.txt",env2);
        Thread t2=new Thread(speedChange);
        t2.start();


        SlowMotion slowMotion = new SlowMotion(kafkaStrm3,"./results/slowMotion.txt",env3);
        Thread t3=new Thread(slowMotion);
        t3.start();


        IncreasingSpeed increasingSpeed = new IncreasingSpeed(kafkaStrm4,"./results/increasingSpeed.txt",env4);
        Thread t4=new Thread(increasingSpeed);
        t4.start();

        UnderWay underWay = new UnderWay(kafkaStrm5, "./results/underWay.txt",env5);
        Thread t5=new Thread(underWay);
        t5.start();

        Drift drift = new Drift(kafkaStrm6,"./results/drift.txt",env6);
        Thread t6=new Thread(drift);
        t6.start();


        Following following = new Following(kafkaStrm7,"./results/following.txt",env7);
        Thread t7=new Thread(following);
        t7.start();

        RendezVousSimple rendezVousSimple = new RendezVousSimple(kafkaStrm8,"./results/randezVous.txt",env7);
        Thread t8=new Thread(rendezVousSimple);
        t8.start();

        //It compiles :)
        System.out.print("Hooooray");

        }
}