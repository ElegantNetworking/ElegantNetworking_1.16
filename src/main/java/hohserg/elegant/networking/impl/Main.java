package hohserg.elegant.networking.impl;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod("elegant_networking")
public class Main {
    public static Logger log = LogManager.getLogger("elegant_networking");
    public static Config config = Init.initConfig(FMLPaths.CONFIGDIR.get().toFile());

    public Main() {
        Init.registerAllPackets(
                ModList.get().getMods()
                        .stream()
                        .map(mod -> new Init.ModInfo(mod.getModId(), mod.getOwningFile().getFile().getFilePath().toFile()))
                        .collect(Collectors.toList()),
                log::info,
                log::error,
                Network.getNetwork()::registerChannel
        );
    }
}
