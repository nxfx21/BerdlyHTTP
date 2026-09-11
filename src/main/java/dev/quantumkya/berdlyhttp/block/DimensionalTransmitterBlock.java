package dev.quantumkya.berdlyhttp.block;

import com.google.gson.JsonObject;
import dev.quantumkya.berdlyhttp.BerdlyConfig;
import dev.quantumkya.berdlyhttp.BerdlyHTTP;
import dev.quantumkya.berdlyhttp.block.entity.DimensionalTransmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DimensionalTransmitterBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final Map<GlobalPos, Long> LAST_TRIGGER = new ConcurrentHashMap<>();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public DimensionalTransmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, Boolean.FALSE)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DimensionalTransmitterBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, LIT);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide()) return;

        boolean hasSignal = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (hasSignal != wasPowered) {
            BlockState newState = state.setValue(POWERED, hasSignal);
            if (hasSignal) {
                boolean newLit = !state.getValue(LIT);
                newState = newState.setValue(LIT, newLit);
                triggerHttp(level, pos);
            }
            level.setBlock(pos, newState, 3);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() && (player.hasPermissions(2) || player.isCreative())) {
            if (level.isClientSide()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        dev.quantumkya.berdlyhttp.client.gui.ClientGuiHelper.openTransmitterScreen(pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private void triggerHttp(Level level, BlockPos pos) {
        String url;
        String method;
        int cooldown;
        boolean sendCoords;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DimensionalTransmitterBlockEntity transmitter) {
            url = transmitter.getEndpointUrl();
            method = transmitter.getHttpMethod();
            cooldown = transmitter.getCooldownTicks();
            sendCoords = transmitter.isSendCoordinates();
        } else {
            url = BerdlyConfig.ENDPOINT_URL.get();
            method = BerdlyConfig.HTTP_METHOD.get();
            cooldown = BerdlyConfig.COOLDOWN_TICKS.get();
            sendCoords = BerdlyConfig.SEND_COORDINATES.get();
        }

        long now = level.getGameTime();
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        Long last = LAST_TRIGGER.get(globalPos);

        if (last == null || now - last >= cooldown) {
            LAST_TRIGGER.put(globalPos, now);
            sendHttpRequest(level, pos, url, method, sendCoords);
        } else {
            BerdlyHTTP.LOGGER.debug("Transmitter at {} throttled by cooldown ({} ticks remaining)",
                    pos, cooldown - (now - last));
        }
    }

    private void sendHttpRequest(Level level, BlockPos pos, String url, String method, boolean sendCoords) {
        if (url == null || url.isBlank()) {
            BerdlyHTTP.LOGGER.warn("Transmitter at {} triggered, but endpoint_url is empty.", pos);
            return;
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            BerdlyHTTP.LOGGER.error("Invalid endpoint URL '{}' on transmitter at {}: {}", url, pos, e.getMessage());
            return;
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(BerdlyConfig.REQUEST_TIMEOUT_SECONDS.get()))
                .header("User-Agent", "BerdlyHTTP/1.0 (Minecraft Forge)");

        String normalizedMethod = method.trim().toUpperCase();
        if ("GET".equals(normalizedMethod)) {
            requestBuilder.GET();
        } else {
            String payload = "";
            if (sendCoords) {
                JsonObject json = new JsonObject();
                json.addProperty("x", pos.getX());
                json.addProperty("y", pos.getY());
                json.addProperty("z", pos.getZ());
                json.addProperty("dimension", level.dimension().location().toString());
                json.addProperty("timestamp", System.currentTimeMillis());
                payload = json.toString();
                requestBuilder.header("Content-Type", "application/json");
            }
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload));
        }

        BerdlyHTTP.LOGGER.info("Transmitter at {} sending {} to {}...", pos, normalizedMethod, uri);

        HTTP_CLIENT.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        BerdlyHTTP.LOGGER.info("Transmitter at {} request succeeded ({})", pos, response.statusCode());
                    } else {
                        BerdlyHTTP.LOGGER.warn("Transmitter at {} request returned non-OK status: {}", pos, response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    BerdlyHTTP.LOGGER.error("Transmitter at {} request failed: {}", pos, ex.getMessage());
                    return null;
                });
    }
}
