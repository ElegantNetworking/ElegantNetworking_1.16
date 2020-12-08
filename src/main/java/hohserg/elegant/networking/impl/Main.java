package hohserg.elegant.networking.impl;

import com.google.common.collect.HashMultimap;
import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import hohserg.elegant.networking.api.ServerToClientPacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Multimaps.toMultimap;
import static java.util.stream.Collectors.*;

@Mod("elegant_networking")
public class Main {
    public static Logger log = LogManager.getLogger("elegant_networking");

    public Main() throws ClassNotFoundException {

        Set<String> channelsToRegister = new HashSet<>();

        Type ElegantPacketType = Type.getType(ElegantPacket.class);
        Type SerializerMarkType = Type.getType(SerializerMark.class);

        HashMultimap<String, ModFileScanData.AnnotationData> annotationsForMod = ModList.get().getAllScanData().stream()
                .flatMap(modFileScanData ->
                        modFileScanData.getAnnotations().stream()
                                .filter(annotationData -> annotationData.getTargetType() == ElementType.TYPE && (annotationData.getAnnotationType().equals(ElegantPacketType) || annotationData.getAnnotationType().equals(SerializerMarkType)))
                                .map(annotationData -> Pair.of(annotationData, modFileScanData.getIModInfoData().get(0).getMods().get(0).getModId()))
                )
                .distinct()
                .collect(toMultimap(Pair::getRight, Pair::getLeft, HashMultimap::create));

        for (Map.Entry<String, Collection<ModFileScanData.AnnotationData>> entry : annotationsForMod.asMap().entrySet()) {
            String modid = entry.getKey();
            Collection<ModFileScanData.AnnotationData> annotations = entry.getValue();
            Map<Type, List<ModFileScanData.AnnotationData>> byType = annotations.stream().collect(groupingBy(ModFileScanData.AnnotationData::getAnnotationType));

            List<ModFileScanData.AnnotationData> rawPackets = byType.get(ElegantPacketType).stream()
                    .filter(a -> {
                        try {
                            return Arrays.stream(Class.forName(a.getMemberName()).getInterfaces()).anyMatch(i -> i == ClientToServerPacket.class || i == ServerToClientPacket.class);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(toList());
            Map<String, Class> rawSerializers = byType.get(SerializerMarkType).stream()
                    .flatMap(a -> {
                        try {
                            return Stream.of(Class.forName(a.getMemberName()));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            return Stream.empty();
                        }
                    })
                    .filter(aClass -> Arrays.stream(aClass.getInterfaces()).anyMatch(i -> i == ISerializer.class))
                    .collect(toMap(
                            cl -> cl.getAnnotation(SerializerMark.class).packetClass().getCanonicalName(),
                            cl -> cl));

            if (rawPackets.size() > 0) {

                List<ElegantNetworking.PacketInfo> packets =
                        rawPackets.stream()
                                .map(a -> new ElegantNetworking.PacketInfo((String) a.getAnnotationData().getOrDefault("channel", modid), a.getMemberName()))
                                .collect(toList());

                packets.stream().map(p -> p.channel).forEach(channelsToRegister::add);

                for (ElegantNetworking.PacketInfo p : packets) {
                    Class maybeSerializer = rawSerializers.get(p.className);
                    if (maybeSerializer != null) {
                        try {
                            ISerializer o = (ISerializer) maybeSerializer.newInstance();
                            log.info("Register packet " + TextFormatting.AQUA + Class.forName(p.className).getSimpleName() + TextFormatting.RESET + " for channel " + TextFormatting.AQUA + p.channel + TextFormatting.RESET + " with id " + o.packetId());
                            ElegantNetworking.register(p, o);
                        } catch (InstantiationException | IllegalAccessException e) {
                            log.error("Unable to instantiate serializer " + maybeSerializer.getName() + " for packet" + TextFormatting.AQUA + Class.forName(p.className).getSimpleName() + TextFormatting.RESET + " for channel " + TextFormatting.AQUA + p.channel);
                            e.printStackTrace();
                        }
                    } else
                        log.error("Not found serializer for packet " + TextFormatting.AQUA + Class.forName(p.className).getSimpleName() + TextFormatting.RESET + " for channel " + TextFormatting.AQUA + p.channel);

                }
            }

        }

        channelsToRegister.forEach(ElegantNetworking.getNetwork()::registerChannel);
    }
}
