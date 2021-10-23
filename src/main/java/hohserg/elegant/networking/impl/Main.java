package hohserg.elegant.networking.impl;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod("elegant_networking")
@Mod.EventBusSubscriber(bus = MOD)
public class Main {
    public static Logger log = LogManager.getLogger("elegant_networking");
    public static Config config = Config.init(FMLPaths.CONFIGDIR.get().toFile());

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        Init.initPackets(log::info, log::warn, Network.getNetwork()::registerChannel, config);
    }
}
