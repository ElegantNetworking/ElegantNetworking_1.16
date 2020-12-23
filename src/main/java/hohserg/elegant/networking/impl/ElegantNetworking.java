package hohserg.elegant.networking.impl;

import lombok.Value;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class ElegantNetworking {

    private static Map<String, String> channelByPacketClassName = new HashMap<>();
    private static Map<String, Integer> packetIdByPacketClassName = new HashMap<>();
    private static Map<Pair<String, Integer>, String> packetClassNameByChannelId = new HashMap<>();
    private static Map<String, ISerializer> serializerByPacketClassName = new HashMap<>();

    public static String getChannelForPacket(String className) {
        return channelByPacketClassName.get(className);
    }

    public static int getPacketId(String className) {
        return packetIdByPacketClassName.get(className);
    }

    public static String getPacketName(String channel, int id) {
        return packetClassNameByChannelId.get(Pair.of(channel, id));
    }

    public static ISerializer getSerializer(String className) {
        return serializerByPacketClassName.get(className);
    }

    private static Network defaultImpl = ModList.get().isLoaded("codechickenlib") ? new CCLNetworkImpl() : new ForgeNetworkImpl();

    public static Network getNetwork() {
        return defaultImpl;
    }

    static void register(PacketInfo p, ISerializer serializer) {
        int id = serializer.packetId();
        channelByPacketClassName.put(p.className, p.channel);
        packetIdByPacketClassName.put(p.className, id);
        packetClassNameByChannelId.put(Pair.of(p.channel, id), p.className);
        serializerByPacketClassName.put(p.className, serializer);
    }

    @Value
    static class PacketInfo {
        public String channel;
        public String className;
    }
}
