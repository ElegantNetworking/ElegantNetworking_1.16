package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ServerToClientPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

public interface Network<PacketRepresentation> {
    void sendToPlayer(ServerToClientPacket serverToClientPacket, ServerPlayerEntity player);

    void sendToClients(ServerToClientPacket serverToClientPacket);

    void sendPacketToAllAround(ServerToClientPacket serverToClientPacket, World world, double x, double y, double z, double range);

    void sendToDimension(ServerToClientPacket serverToClientPacket, World world);

    void sendToChunk(ServerToClientPacket serverToClientPacket, World world, int chunkX, int chunkZ);

    void sendToServer(ClientToServerPacket clientToServerPacket);

    void onReceiveClient(PacketRepresentation packetRepresent, String channel);

    void onReceiveServer(PacketRepresentation packetRepresent, ServerPlayerEntity player, String channel);

    void registerChannel(String channel);

}
