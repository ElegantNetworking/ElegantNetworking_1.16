package hohserg.elegant.networking.impl;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustomChannelBuilder;
import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.IByteBufSerializable;
import hohserg.elegant.networking.api.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CCLNetworkImpl implements Network<PacketCustom> {
    @Override
    public void sendToPlayer(ServerToClientPacket serverToClientPacket, ServerPlayerEntity player) {
        preparePacket(serverToClientPacket).sendToPlayer(player);
    }

    @Override
    public void sendToClients(ServerToClientPacket serverToClientPacket) {
        preparePacket(serverToClientPacket).sendToClients();
    }

    @Override
    public void sendPacketToAllAround(ServerToClientPacket serverToClientPacket, World world, double x, double y, double z, double range) {
        preparePacket(serverToClientPacket).sendPacketToAllAround(x, y, z, range, world.getDimensionKey());
    }

    @Override
    public void sendToDimension(ServerToClientPacket serverToClientPacket, World world) {
        preparePacket(serverToClientPacket).sendToDimension(world.getDimensionKey());
    }

    @Override
    public void sendToChunk(ServerToClientPacket serverToClientPacket, World world, int chunkX, int chunkZ) {
        preparePacket(serverToClientPacket).sendToChunk(world, chunkX, chunkZ);
    }

    @Override
    public void sendToServer(ClientToServerPacket packet) {
        preparePacket(packet).sendToServer();
    }

    private PacketCustom preparePacket(IByteBufSerializable packet) {
        checkSendingSide(packet);

        String packetClassName = packet.getClass().getName();
        ISerializerBase<IByteBufSerializable> serializer = Registry.getSerializer(packetClassName);
        String channel = Registry.getChannelForPacket(packetClassName);
        Integer id = Registry.getPacketId(packetClassName);
        PacketCustom packetCustom = new PacketCustom(new ResourceLocation(channel, channel), id);

        ByteBuf buffer = Unpooled.buffer();
        serializer.serialize(packet, buffer);
        packetCustom.writeBytes(buffer.array(), 0, buffer.readableBytes());

        return packetCustom;
    }

    @Override
    public void onReceiveClient(PacketCustom packetRepresent, String channel) {
        this.<ServerToClientPacket>readObjectFromPacket(packetRepresent, channel)
                .onReceive(Minecraft.getInstance());
    }

    @Override
    public void onReceiveServer(PacketCustom packetRepresent, ServerPlayerEntity player, String channel) {
        this.<ClientToServerPacket>readObjectFromPacket(packetRepresent, channel)
                .onReceive(player);
    }

    private <A> A readObjectFromPacket(PacketCustom packetRepresent, String channel) {
        return (A) Registry.getSerializer(Registry.getPacketName(channel, packetRepresent.getType())).unserialize(packetRepresent.readByteBuf());
    }

    @Override
    public void registerChannel(String channel) {
        PacketCustomChannelBuilder.named(new ResourceLocation(channel, channel))
                .assignClientHandler(() -> () -> (ICustomPacketHandler.IClientPacketHandler) (packet, mc, handler) -> onReceiveClient(packet, channel))
                .assignServerHandler(() -> () -> (ICustomPacketHandler.IServerPacketHandler) (packet, sender, handler) -> onReceiveServer(packet, sender, channel))
                .build();
    }
}
