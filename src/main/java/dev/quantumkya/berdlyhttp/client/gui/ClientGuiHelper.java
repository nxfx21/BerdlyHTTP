package dev.quantumkya.berdlyhttp.client.gui;

import dev.quantumkya.berdlyhttp.block.entity.DimensionalTransmitterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

@SuppressWarnings("null")
public final class ClientGuiHelper {
    private ClientGuiHelper() {}

    public static void openTransmitterScreen(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        BlockEntity be = mc.level.getBlockEntity(pos);
        if (be instanceof DimensionalTransmitterBlockEntity transmitter) {
            mc.setScreen(new TransmitterConfigScreen(
                    pos,
                    transmitter.getRawEndpointUrl(),
                    transmitter.getRawHttpMethod(),
                    transmitter.getRawCooldownTicks(),
                    transmitter.getRawSendCoordinates()
            ));
        }
    }
}
