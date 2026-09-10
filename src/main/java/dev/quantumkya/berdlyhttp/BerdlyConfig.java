package dev.quantumkya.berdlyhttp;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BerdlyConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> ENDPOINT_URL;
    public static final ForgeConfigSpec.ConfigValue<String> HTTP_METHOD;
    public static final ForgeConfigSpec.BooleanValue SEND_COORDINATES;
    public static final ForgeConfigSpec.IntValue COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue REQUEST_TIMEOUT_SECONDS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("BerdlyHTTP Configuration").push("general");

        ENDPOINT_URL = builder
                .comment("Target URL to ping")
                .define("endpoint_url", "https://berdsmp.quantumkya.dev/somewhere");

        HTTP_METHOD = builder
                .comment("HTTP method (GET or POST)")
                .define("http_method", "POST");

        SEND_COORDINATES = builder
                .comment("Send block position and dimension in POST body")
                .define("send_coordinates", true);

        COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks between triggers (20 ticks = 1s)")
                .defineInRange("cooldown_ticks", 20, 0, 72000);

        REQUEST_TIMEOUT_SECONDS = builder
                .comment("Request timeout in seconds")
                .defineInRange("request_timeout_seconds", 5, 1, 60);

        builder.pop();
        SPEC = builder.build();
    }

    private BerdlyConfig() {}
}
