package dev.quantumkya.berdlyhttp.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Locale;

@SuppressWarnings("null")
public class TransmitterConfigScreen extends Screen {
    private final BlockPos pos;
    private final String initialUrl;
    private final int initialCooldown;

    private String currentMethod;
    private boolean currentSendCoords;

    private EditBox urlBox;
    private EditBox cooldownBox;
    private Button methodButton;
    private Button coordsButton;

    public TransmitterConfigScreen(BlockPos pos, String initialUrl, String initialMethod, int initialCooldown, boolean initialSendCoords) {
        super(Component.translatable("gui.berdlyhttp.transmitter_config.title"));
        this.pos = pos;
        this.initialUrl = initialUrl != null ? initialUrl : "";
        this.currentMethod = (initialMethod != null && !initialMethod.isEmpty()) ? initialMethod.toUpperCase(Locale.ROOT) : "POST";
        this.initialCooldown = initialCooldown >= 0 ? initialCooldown : 20;
        this.currentSendCoords = initialSendCoords;
    }

    @Override
    protected void init() {
        int boxWidth = 240;
        int left = (this.width - boxWidth) / 2;
        int startY = 45;

        this.urlBox = new EditBox(this.font, left, startY, boxWidth, 20, Component.translatable("gui.berdlyhttp.transmitter_config.url"));
        this.urlBox.setMaxLength(512);
        this.urlBox.setValue(this.initialUrl);
        this.addRenderableWidget(this.urlBox);

        this.methodButton = Button.builder(
                Component.translatable("gui.berdlyhttp.transmitter_config.method").append(this.currentMethod),
                btn -> {
                    this.currentMethod = "POST".equalsIgnoreCase(this.currentMethod) ? "GET" : "POST";
                    btn.setMessage(Component.translatable("gui.berdlyhttp.transmitter_config.method").append(this.currentMethod));
                })
                .bounds(left, startY + 32, boxWidth, 20)
                .build();
        this.addRenderableWidget(this.methodButton);

        this.cooldownBox = new EditBox(this.font, left, startY + 76, boxWidth, 20, Component.translatable("gui.berdlyhttp.transmitter_config.cooldown"));
        this.cooldownBox.setMaxLength(10);
        this.cooldownBox.setValue(String.valueOf(this.initialCooldown));
        this.addRenderableWidget(this.cooldownBox);

        this.coordsButton = Button.builder(
                Component.translatable("gui.berdlyhttp.transmitter_config.coords").append(this.currentSendCoords ? "ON" : "OFF"),
                btn -> {
                    this.currentSendCoords = !this.currentSendCoords;
                    btn.setMessage(Component.translatable("gui.berdlyhttp.transmitter_config.coords").append(this.currentSendCoords ? "ON" : "OFF"));
                })
                .bounds(left, startY + 108, boxWidth, 20)
                .build();
        this.addRenderableWidget(this.coordsButton);

        int btnWidth = (boxWidth - 10) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.berdlyhttp.transmitter_config.save"), btn -> saveAndClose())
                .bounds(left, startY + 140, btnWidth, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.berdlyhttp.transmitter_config.cancel"), btn -> this.onClose())
                .bounds(left + btnWidth + 10, startY + 140, btnWidth, 20)
                .build());
    }

    private void saveAndClose() {
        String url = this.urlBox.getValue().trim();
        int cooldown;
        try {
            cooldown = Integer.parseInt(this.cooldownBox.getValue().trim());
        } catch (NumberFormatException e) {
            cooldown = 20;
        }

        String cmd = String.format(Locale.ROOT, "berdlyhttp config set %d %d %d %s %d %b %s",
                this.pos.getX(), this.pos.getY(), this.pos.getZ(),
                this.currentMethod, cooldown, this.currentSendCoords, url);

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.connection.sendCommand(cmd);
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFF);

        int boxWidth = 240;
        int left = (this.width - boxWidth) / 2;
        int startY = 45;

        guiGraphics.drawString(this.font, Component.translatable("gui.berdlyhttp.transmitter_config.url"), left, startY - 12, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gui.berdlyhttp.transmitter_config.cooldown"), left, startY + 64, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
