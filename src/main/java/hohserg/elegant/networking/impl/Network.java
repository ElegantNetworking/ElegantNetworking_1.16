package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.IByteBufSerializable;
import hohserg.elegant.networking.api.ServerToClientPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;

public interface Network<PacketRepresentation> {

    Network defaultImpl = ModList.get().isLoaded("codechickenlib") ? new CCLNetworkImpl() : new ForgeNetworkImpl();

    static Network getNetwork() {
        return defaultImpl;
    }

    void sendToPlayer(ServerToClientPacket serverToClientPacket, ServerPlayerEntity player);

    void sendToClients(ServerToClientPacket serverToClientPacket);

    void sendPacketToAllAround(ServerToClientPacket serverToClientPacket, World world, double x, double y, double z, double range);

    void sendToDimension(ServerToClientPacket serverToClientPacket, World world);

    void sendToChunk(ServerToClientPacket serverToClientPacket, World world, int chunkX, int chunkZ);

    void sendToServer(ClientToServerPacket clientToServerPacket);

    void onReceiveClient(PacketRepresentation packetRepresent, String channel);

    void onReceiveServer(PacketRepresentation packetRepresent, ServerPlayerEntity player, String channel);

    void registerChannel(String channel);

    default void checkSendingSide(IByteBufSerializable packet) {
        Dist side = FMLEnvironment.dist;

        if (side == Dist.CLIENT && packet instanceof ServerToClientPacket)
            throw new RuntimeException("Attempt to send ServerToClientPacket from client side: " + packet.getClass().getCanonicalName());
        else if (side == Dist.DEDICATED_SERVER && packet instanceof ClientToServerPacket)
            throw new RuntimeException("Attempt to send ClientToServerPacket from server side: " + packet.getClass().getCanonicalName());
    }


}
