package hohserg.elegant.networking.api;

import io.netty.buffer.ByteBuf;

/**
 * Base interface for serializable packets
 * <p>
 * Optional: constructor with one ByteBuf argument
 * will be used for unserialize packet
 */
public interface IByteBufSerializable {

    /**
     * Serialize this packet to ByteBuf
     * for send by net
     */
    default void serialize(ByteBuf acc) {
        throw new UnsupportedOperationException("Default implementation: need to override serialize method and unserialization constructor");
    }

}
