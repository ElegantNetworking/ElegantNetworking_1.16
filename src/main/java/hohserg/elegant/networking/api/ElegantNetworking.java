package hohserg.elegant.networking.api;

import hohserg.elegant.networking.impl.ISerializerBase;
import hohserg.elegant.networking.impl.Registry;

public class ElegantNetworking {
    public static <A extends IByteBufSerializable> ISerializerBase<A> getByteBufSerializerFor(Class<A> serializable) {
        return Registry.getSerializerFor(serializable);
    }

    public static <A extends IByteBufSerializable> NbtSerializer<A> getNbtSerializerFor(Class<A> serializable) {
        ISerializerBase<A> serializer = getByteBufSerializerFor(serializable);
        return new NbtSerializer<>(serializer);
    }
}
