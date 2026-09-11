package dev.quantumkya.berdlyhttp.block;

import dev.quantumkya.berdlyhttp.BerdlyHTTP;
import dev.quantumkya.berdlyhttp.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BerdlyHTTP.MOD_ID);

    public static final RegistryObject<Block> DIMENSIONAL_TRANSMITTER = registerBlock("dimensional_transmitter",
            () -> new DimensionalTransmitterBlock(BlockBehaviour.Properties.of()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> state.getValue(DimensionalTransmitterBlock.POWERED) ? 4 : 0)
                    .requiresCorrectToolForDrops()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
