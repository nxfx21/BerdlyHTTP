package dev.quantumkya.berdlyhttp;

import com.mojang.logging.LogUtils;
import dev.quantumkya.berdlyhttp.block.ModBlocks;
import dev.quantumkya.berdlyhttp.block.entity.ModBlockEntities;
import dev.quantumkya.berdlyhttp.command.BerdlyCommands;
import dev.quantumkya.berdlyhttp.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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
        ModBlockEntities.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        context.registerConfig(ModConfig.Type.SERVER, BerdlyConfig.SPEC, "berdlyhttp-server.toml");

        LOGGER.info("BerdlyHTTP loaded and it's alright :]");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.DIMENSIONAL_TRANSMITTER);
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        BerdlyCommands.register(event.getDispatcher());
    }
}