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

@Mod(BerdlyHTTP.MOD_ID)
public final class BerdlyHTTP {
    public static final String MOD_ID = "berdlyHttp";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String API_URL = "berdsmp.quantumkya.dev/somewhere";

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

        // logic only runs server-side because the server has the frickin plugin
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        if (level.getBlockState(pos).is(ModBlocks.DIMENSIONAL_TRANSMITTER.get())) {

            if (level.getBestNeighborSignal(pos) > 0) LOGGER.info("Dimensional transmitter activated! Sending data...");
            else return;

            MinecraftServer server = level.getServer();
            if (server == null) return;

            // execute command
            String command = "httprequest " + API_URL;
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
        }
    }
}