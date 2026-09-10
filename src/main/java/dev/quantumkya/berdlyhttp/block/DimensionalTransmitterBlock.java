package dev.quantumkya.berdlyhttp.block;

import dev.quantumkya.berdlyhttp.BerdlyConfig;
import dev.quantumkya.berdlyhttp.BerdlyHTTP;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Future survival crafting recipe idea:
// [Redstone, Amethyst Shards, Iron Ingot, Ender Pearl]
public class DimensionalTransmitterBlock extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final Map<BlockPos, Long> LAST_TRIGGER = new ConcurrentHashMap<>();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public DimensionalTransmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        boolean hasSignal = level.hasNeighborSignal(pos);
        boolean isPowered = state.getValue(POWERED);

        if (hasSignal != isPowered) {
            if (hasSignal) {
                long now = level.getGameTime();
                Long last = LAST_TRIGGER.get(pos);
                int cooldown = BerdlyConfig.COOLDOWN_TICKS.get();

                if (last == null || now - last >= cooldown) {
                    LAST_TRIGGER.put(pos, now);
                    sendHttpRequest(level, pos);
                } else {
                    BerdlyHTTP.LOGGER.debug("Transmitter at {} throttled by cooldown ({} ticks remaining)",
                            pos, cooldown - (now - last));
                }
            }
            level.setBlock(pos, state.setValue(POWERED, hasSignal), 2);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            LAST_TRIGGER.remove(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void sendHttpRequest(Level level, BlockPos pos) {
        String url = BerdlyConfig.ENDPOINT_URL.get();
        if (url == null || url.isBlank()) {
            BerdlyHTTP.LOGGER.warn("Dimensional transmitter at {} activated, but endpoint_url is empty in config.", pos);
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            BerdlyHTTP.LOGGER.error("Invalid endpoint URL '{}' in BerdlyHTTP config: {}", url, e.getMessage());
            return;
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(BerdlyConfig.REQUEST_TIMEOUT_SECONDS.get()))
                .header("User-Agent", "BerdlyHTTP/1.0 (Minecraft Forge)");

        String method = BerdlyConfig.HTTP_METHOD.get().trim().toUpperCase();
        if ("GET".equals(method)) {
            requestBuilder.GET();
        } else {
            String payload = "";
            if (BerdlyConfig.SEND_COORDINATES.get()) {
                payload = String.format(
                        "{\"x\":%d,\"y\":%d,\"z\":%d,\"dimension\":\"%s\",\"timestamp\":%d}",
                        pos.getX(), pos.getY(), pos.getZ(),
                        level.dimension().location().toString(),
                        System.currentTimeMillis()
                );
                requestBuilder.header("Content-Type", "application/json");
            }
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload));
        }

        BerdlyHTTP.LOGGER.info("Dimensional transmitter at {} sending {} to {}...", pos, method, uri);

        HTTP_CLIENT.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        BerdlyHTTP.LOGGER.info("Transmitter at {} request succeeded (status {})", pos, response.statusCode());
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
