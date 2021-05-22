package hohserg.elegant.networking.api;

import hohserg.elegant.networking.impl.ISerializerBase;
import hohserg.elegant.networking.impl.Registry;

public class ElegantNetworking {
    /**
     * Gives a serializer for requested type.
     * Serializer generally is pair of functions:
     *
     * {@code A => ByteBuf }
     * {@code ByteBuf => A }
     *
     * @param <A>          requested type
     * @param serializable class of requested type
     * @return serializer
     */
    public static <A extends IByteBufSerializable> ISerializerBase<A> getByteBufSerializerFor(Class<A> serializable) {
        return Registry.getSerializerFor(serializable);
    }

    /**
     * Gives a nbt serializer for requested type.
     * NbtSerializer is tetrad  of functions:
     *
     * {@code A => NBTTagCompound }
     * {@code NBTTagCompound => A }
     *
     * {@code A => NBTTagByteArray }
     * {@code NBTTagByteArray => A }
     *
     * Useful for capability storage, world save data and etc
     *
     * @param <A>          requested type
     * @param serializable class of requested type
     * @return serializer
     */
    public static <A extends IByteBufSerializable> NbtSerializer<A> getNbtSerializerFor(Class<A> serializable) {
        ISerializerBase<A> serializer = getByteBufSerializerFor(serializable);
        return new NbtSerializer<>(serializer);
    }
}
