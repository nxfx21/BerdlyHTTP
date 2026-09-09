package com.quantumkya.berdlyHttp;

import com.mojang.logging.LogUtils;
import com.quantumkya.berdlyHttp.block.ModBlocks;
import com.quantumkya.berdlyHttp.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BerdlyHTTP.MODID)
public final class BerdlyHTTP {
    public static final String MODID = "berdlyHttp";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BerdlyHTTP() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info("Initializing BerdlyHTTP...");

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("BerdlyHTTP loaded and it's alright :]");
    }

    @SubscribeEvent
    public void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        Level level = (Level) event.getLevel();

        // Ensure this logic only runs on the Server side, not the Client side
        if (!level.isClientSide()) {
            BlockPos pos = event.getPos();

            // 1. Check if the block receiving power is your target block type
            // (Replace 'DETECTOR_RAIL' with whatever block you are using)
            if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DETECTOR_RAIL)) {

                // 2. Check if the block is currently receiving redstone power
                if (level.getBestNeighborSignal(pos) > 0) {
                    LOGGER.info("Custom redstone block activated! Sending HTTP request command...");

                    // 3. Get the server instance and execute the plugin's command
                    MinecraftServer server = level.getServer();
                    if (server != null) {
                        String targetUrl = "https://example.com";

                        // This executes the command exactly as if it were typed in the server console
                        String command = "httprequest " + targetUrl;
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
                    }
                }
            }
        }
    }
}