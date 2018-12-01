package team;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

/**
 * Implements a SerializationSchema and DeserializationSchema for TaxiRide for Kafka data sources and sinks.
 */
public class DynamicShipSchema  implements DeserializationSchema<DynamicShipClass>, SerializationSchema<DynamicShipClass> {

    @Override
    public byte[] serialize(DynamicShipClass element) {
        return element.toString().getBytes();
    }

    @Override
    public DynamicShipClass deserialize(byte[] message) {
        return DynamicShipClass.fromString(new String(message));
    }

    @Override
    public boolean isEndOfStream(DynamicShipClass nextElement) {
        return false;
    }

    @Override
    public TypeInformation<DynamicShipClass> getProducedType() {
        return TypeExtractor.getForClass(DynamicShipClass.class);
    }
}

