package hohserg.elegant.networking.api;

import hohserg.elegant.networking.impl.ElegantNetworking;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * Base interface for packet, which can be send from client to server
 */
public interface ClientToServerPacket extends IByteBufSerializable {
    /**
     * Called when the packet is received
     *
     * @param player Sender
     */
    void onReceive(ServerPlayerEntity player);

    /**
     * Use it for send packet instance to server
     */
    default void sendToServer() {
        ElegantNetworking.getNetwork().sendToServer(this);
    }
}
