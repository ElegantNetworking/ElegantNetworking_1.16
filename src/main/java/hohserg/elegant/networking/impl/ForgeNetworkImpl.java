package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.IByteBufSerializable;
import hohserg.elegant.networking.api.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;

public class ForgeNetworkImpl implements Network<ForgeNetworkImpl.UniversalPacket> {
    @Override
    public void sendToPlayer(ServerToClientPacket serverToClientPacket, ServerPlayerEntity player) {
        getChannel(serverToClientPacket).send(PacketDistributor.PLAYER.with(() -> player), preparePacket(serverToClientPacket));
    }

    private SimpleChannel getChannel(IByteBufSerializable packet) {
        checkSendingSide(packet);
        return channels.get(Registry.getChannelForPacket(packet.getClass().getName()));
    }

    @Override
    public void sendToClients(ServerToClientPacket serverToClientPacket) {
        getChannel(serverToClientPacket).send(PacketDistributor.ALL.noArg(), preparePacket(serverToClientPacket));
    }

    @Override
    public void sendPacketToAllAround(ServerToClientPacket serverToClientPacket, World world, double x, double y, double z, double range) {
        getChannel(serverToClientPacket).send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, range, world.getDimensionKey())), preparePacket(serverToClientPacket));
    }

    @Override
    public void sendToDimension(ServerToClientPacket serverToClientPacket, World world) {
        getChannel(serverToClientPacket).send(PacketDistributor.DIMENSION.with(world::getDimensionKey), preparePacket(serverToClientPacket));
    }

    @Override
    public void sendToChunk(ServerToClientPacket serverToClientPacket, World world, int chunkX, int chunkZ) {
        getChannel(serverToClientPacket).send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(chunkX, chunkZ)), preparePacket(serverToClientPacket));
    }

    @Override
    public void sendToServer(ClientToServerPacket packet) {
        getChannel(packet).sendToServer(preparePacket(packet));
    }

    private ServerToClientUniversalPacket preparePacket(ServerToClientPacket packet) {
        return new ServerToClientUniversalPacket(Registry.getPacketId(packet.getClass().getName()), packet);
    }

    private ClientToServerUniversalPacket preparePacket(ClientToServerPacket packet) {
        return new ClientToServerUniversalPacket(Registry.getPacketId(packet.getClass().getName()), packet);
    }

    @Override
    public void onReceiveClient(UniversalPacket packetRepresent, String channel) {
        this.<ServerToClientPacket>readObjectFromPacket(packetRepresent)
                .onReceive(Minecraft.getInstance());
    }

    @Override
    public void onReceiveServer(UniversalPacket packetRepresent, ServerPlayerEntity player, String channel) {
        this.<ClientToServerPacket>readObjectFromPacket(packetRepresent)
                .onReceive(player);
    }

    private <A> A readObjectFromPacket(UniversalPacket packetRepresent) {
        return (A) packetRepresent.packet;
    }

    private Map<String, SimpleChannel> channels = new HashMap<>();

    private final String NET_COMM_VERSION = "0";

    @Override
    public void registerChannel(String channel) {
        SimpleChannel simpleNetworkWrapper = NetworkRegistry.newSimpleChannel(new ResourceLocation(channel, channel), () -> NET_COMM_VERSION, NET_COMM_VERSION::equals, NET_COMM_VERSION::equals);
        channels.put(channel, simpleNetworkWrapper);

        simpleNetworkWrapper.registerMessage(0, ServerToClientUniversalPacket.class,
                UniversalPacket::toBytes,
                packetBuffer -> {
                    ServerToClientUniversalPacket packetRepr = new ServerToClientUniversalPacket();
                    packetRepr.fromBytes(packetBuffer, channel);
                    return packetRepr;
                },
                (serverToClientUniversalPacket, contextSupplier) -> {
                    contextSupplier.get().enqueueWork(() -> onReceiveClient(serverToClientUniversalPacket, channel));
                    contextSupplier.get().setPacketHandled(true);
                });

        simpleNetworkWrapper.registerMessage(1, ClientToServerUniversalPacket.class,
                UniversalPacket::toBytes,
                packetBuffer -> {
                    ClientToServerUniversalPacket packetRepr = new ClientToServerUniversalPacket();
                    packetRepr.fromBytes(packetBuffer, channel);
                    return packetRepr;
                },
                (clientToServerUniversalPacket, contextSupplier) -> {
                    contextSupplier.get().enqueueWork(() -> onReceiveServer(clientToServerUniversalPacket, contextSupplier.get().getSender(), channel));
                    contextSupplier.get().setPacketHandled(true);
                });
    }

    @NoArgsConstructor
    static class ClientToServerUniversalPacket extends UniversalPacket<ClientToServerPacket> {
        ClientToServerUniversalPacket(int id, ClientToServerPacket packet) {
            super(id, packet);
        }
    }

    @NoArgsConstructor
    static class ServerToClientUniversalPacket extends UniversalPacket<ServerToClientPacket> {
        ServerToClientUniversalPacket(int id, ServerToClientPacket packet) {
            super(id, packet);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    static class UniversalPacket<A extends IByteBufSerializable> {

        private int id;
        A packet;


        void fromBytes(ByteBuf buf, String channel) {
            id = buf.readByte();
            String packetName = Registry.getPacketName(channel, id);
            packet = (A) Registry.getSerializer(packetName).unserialize(buf);
        }

        void toBytes(ByteBuf buf) {
            buf.writeByte(id);
            Registry.getSerializer(packet.getClass().getName()).serialize(packet, buf);
        }
    }
}
