package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.IByteBufSerializable;
import hohserg.elegant.networking.api.ServerToClientPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.loading.FMLEnvironment;

public interface Network<PacketRepresentation> {

    Network defaultImpl =
            Main.config.getBackgroundPacketSystem() == Config.BackgroundPacketSystem.CCLImpl ?
                    ModList.get().isLoaded("codechickenlib") ?
                            new CCLNetworkImpl() :
                            throwMissingCCL()
                    :
                    new ForgeNetworkImpl();

    static Network throwMissingCCL() {
        throw new RuntimeException("Missed CodeChickenLib which required by elegant_networking.cfg");
    }

    static Network getNetwork() {
        return defaultImpl;
    }

    void sendToPlayer(ServerToClientPacket packet, ServerPlayerEntity player);

    void sendToClients(ServerToClientPacket packet);

    void sendPacketToAllAround(ServerToClientPacket packet, World world, double x, double y, double z, double range);

    void sendToDimension(ServerToClientPacket packet, World world);

    void sendToChunk(ServerToClientPacket packet, World world, int chunkX, int chunkZ);

    void sendToServer(ClientToServerPacket packet);

    void onReceiveClient(PacketRepresentation packetRepresent, String channel);

    void onReceiveServer(PacketRepresentation packetRepresent, ServerPlayerEntity player, String channel);

    void registerChannel(String channel);

    default void checkSendingSide(ServerToClientPacket packet) {
        if (EffectiveSide.get() == LogicalSide.CLIENT)
            throw new RuntimeException("Attempt to send ServerToClientPacket from client side: " + packet.getClass().getCanonicalName());
    }

    default void checkSendingSide(ClientToServerPacket packet) {
        if (EffectiveSide.get() == LogicalSide.SERVER)
            throw new RuntimeException("Attempt to send ClientToServerPacket from server side: " + packet.getClass().getCanonicalName());
    }


}
