package dev.quantumkya.berdlyhttp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.quantumkya.berdlyhttp.BerdlyConfig;
import dev.quantumkya.berdlyhttp.block.entity.DimensionalTransmitterBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class BerdlyCommands {
    private BerdlyCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("berdlyhttp")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            // Reload config from disk
                            BerdlyConfig.SPEC.afterReload();
                            ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.reload_success"), true);
                            return 1;
                        }))
                .then(Commands.literal("config")
                        // /berdlyhttp config set <pos> <method> <cooldown> <sendCoords> <url>
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("method", StringArgumentType.word())
                                                .then(Commands.argument("cooldown", IntegerArgumentType.integer(0, 72000))
                                                        .then(Commands.argument("coords", BoolArgumentType.bool())
                                                                .then(Commands.argument("url", StringArgumentType.greedyString())
                                                                        .executes(ctx -> {
                                                                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                                                            String method = StringArgumentType.getString(ctx, "method");
                                                                            int cooldown = IntegerArgumentType.getInteger(ctx, "cooldown");
                                                                            boolean coords = BoolArgumentType.getBool(ctx, "coords");
                                                                            String url = StringArgumentType.getString(ctx, "url");

                                                                            BlockEntity be = ctx.getSource().getLevel().getBlockEntity(pos);
                                                                            if (be instanceof DimensionalTransmitterBlockEntity transmitter) {
                                                                                transmitter.updateAll(url, method, cooldown, coords);
                                                                                ctx.getSource().sendSuccess(() ->
                                                                                        Component.translatable("command.berdlyhttp.success", pos.toShortString()), true);
                                                                                return 1;
                                                                            } else {
                                                                                ctx.getSource().sendFailure(Component.translatable("command.berdlyhttp.target_not_found"));
                                                                                return 0;
                                                                            }
                                                                        })))))))
                        // /berdlyhttp config default <endpoint|method|cooldown|coords> <value>
                        .then(Commands.literal("default")
                                .then(Commands.literal("endpoint")
                                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String url = StringArgumentType.getString(ctx, "url");
                                                    BerdlyConfig.ENDPOINT_URL.set(url);
                                                    BerdlyConfig.SPEC.save();
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.default_success", "endpoint", url), true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("method")
                                        .then(Commands.argument("method", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String method = StringArgumentType.getString(ctx, "method").toUpperCase();
                                                    BerdlyConfig.HTTP_METHOD.set(method);
                                                    BerdlyConfig.SPEC.save();
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.default_success", "method", method), true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("cooldown")
                                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0, 72000))
                                                .executes(ctx -> {
                                                    int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                                    BerdlyConfig.COOLDOWN_TICKS.set(ticks);
                                                    BerdlyConfig.SPEC.save();
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.default_success", "cooldown", String.valueOf(ticks)), true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("coords")
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                                    BerdlyConfig.SEND_COORDINATES.set(enabled);
                                                    BerdlyConfig.SPEC.save();
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.default_success", "coords", String.valueOf(enabled)), true);
                                                    return 1;
                                                }))))
                        // /berdlyhttp config target <endpoint|method|cooldown|coords> <value>
                        .then(Commands.literal("target")
                                .then(Commands.literal("endpoint")
                                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    DimensionalTransmitterBlockEntity be = getTargetTransmitter(player);
                                                    if (be != null) {
                                                        String url = StringArgumentType.getString(ctx, "url");
                                                        be.setEndpointUrl(url);
                                                        ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.success", be.getBlockPos().toShortString()), true);
                                                        return 1;
                                                    }
                                                    ctx.getSource().sendFailure(Component.translatable("command.berdlyhttp.target_not_found"));
                                                    return 0;
                                                })))
                                .then(Commands.literal("method")
                                        .then(Commands.argument("method", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    DimensionalTransmitterBlockEntity be = getTargetTransmitter(player);
                                                    if (be != null) {
                                                        String method = StringArgumentType.getString(ctx, "method").toUpperCase();
                                                        be.setHttpMethod(method);
                                                        ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.success", be.getBlockPos().toShortString()), true);
                                                        return 1;
                                                    }
                                                    ctx.getSource().sendFailure(Component.translatable("command.berdlyhttp.target_not_found"));
                                                    return 0;
                                                })))
                                .then(Commands.literal("cooldown")
                                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0, 72000))
                                                .executes(ctx -> {
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    DimensionalTransmitterBlockEntity be = getTargetTransmitter(player);
                                                    if (be != null) {
                                                        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                                        be.setCooldownTicks(ticks);
                                                        ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.success", be.getBlockPos().toShortString()), true);
                                                        return 1;
                                                    }
                                                    ctx.getSource().sendFailure(Component.translatable("command.berdlyhttp.target_not_found"));
                                                    return 0;
                                                })))
                                .then(Commands.literal("coords")
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    DimensionalTransmitterBlockEntity be = getTargetTransmitter(player);
                                                    if (be != null) {
                                                        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                                        be.setSendCoordinates(enabled);
                                                        ctx.getSource().sendSuccess(() -> Component.translatable("command.berdlyhttp.success", be.getBlockPos().toShortString()), true);
                                                        return 1;
                                                    }
                                                    ctx.getSource().sendFailure(Component.translatable("command.berdlyhttp.target_not_found"));
                                                    return 0;
                                                }))))));
    }

    private static DimensionalTransmitterBlockEntity getTargetTransmitter(ServerPlayer player) {
        HitResult hit = player.pick(10.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            BlockEntity be = player.level().getBlockEntity(blockHit.getBlockPos());
            if (be instanceof DimensionalTransmitterBlockEntity transmitter) {
                return transmitter;
            }
        }
        return null;
    }
}
