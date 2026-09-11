package dev.quantumkya.berdlyhttp.block.entity;

import dev.quantumkya.berdlyhttp.BerdlyHTTP;
import dev.quantumkya.berdlyhttp.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BerdlyHTTP.MOD_ID);

    public static final RegistryObject<BlockEntityType<DimensionalTransmitterBlockEntity>> DIMENSIONAL_TRANSMITTER_BE =
            BLOCK_ENTITIES.register("dimensional_transmitter",
                    () -> BlockEntityType.Builder.of(DimensionalTransmitterBlockEntity::new,
                            ModBlocks.DIMENSIONAL_TRANSMITTER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
