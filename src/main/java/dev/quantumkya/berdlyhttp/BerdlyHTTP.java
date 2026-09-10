package dev.quantumkya.berdlyhttp;

import com.mojang.logging.LogUtils;
import dev.quantumkya.berdlyhttp.block.ModBlocks;
import dev.quantumkya.berdlyhttp.item.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BerdlyHTTP.MOD_ID)
public final class BerdlyHTTP {
    public static final String MOD_ID = "berdlyhttp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BerdlyHTTP(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        LOGGER.info("Initializing BerdlyHTTP...");

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        context.registerConfig(ModConfig.Type.SERVER, BerdlyConfig.SPEC, "berdlyhttp-server.toml");

        LOGGER.info("BerdlyHTTP loaded and it's alright :]");
    }
}