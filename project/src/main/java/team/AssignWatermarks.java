package team;

import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

public class AssignWatermarks implements AssignerWithPunctuatedWatermarks<DynamicShipClass> {
    @Override
    public long extractTimestamp(DynamicShipClass event, long previousElementTimestamp) {
        return event.getEventTime();
    }

    @Override
    public Watermark checkAndGetNextWatermark(DynamicShipClass event, long extractedTimestamp) {
        // simply emit a watermark with every event
        return new Watermark(extractedTimestamp - 30000);
    }
}