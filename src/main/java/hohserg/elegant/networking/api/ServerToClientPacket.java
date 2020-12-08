package hohserg.elegant.networking.api;

import hohserg.elegant.networking.impl.ElegantNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Base interface for packet, which can be send from server to client
 */
public interface ServerToClientPacket extends IByteBufSerializable {
    /**
     * Called when the packet is received
     *
     * @param mc Minecraft class instance
     */
    void onReceive(Minecraft mc);

    /**
     * Use it for send packet instance to concrete player
     */
    default void sendToPlayer(ServerPlayerEntity player) {
        ElegantNetworking.getNetwork().sendToPlayer(this, player);
    }

    /**
     * Use it for send packet instance to all players
     */
    default void sendToClients() {
        ElegantNetworking.getNetwork().sendToClients(this);

    }

    /**
     * Use it for send packet instance to all players around coordinates
     */
    default void sendPacketToAllAround(World world, double x, double y, double z, double range) {
        ElegantNetworking.getNetwork().sendPacketToAllAround(this, world, x, y, z, range);
    }

    /**
     * Use it for send packet instance to all players in concrete dimension
     */
    default void sendToDimension(World world) {
        ElegantNetworking.getNetwork().sendToDimension(this, world);
    }

    /**
     * Use it for send packet instance to all players in concrete chunk
     */
    default void sendToChunk(World world, int chunkX, int chunkZ) {
        ElegantNetworking.getNetwork().sendToChunk(this, world, chunkX, chunkZ);
    }
}
