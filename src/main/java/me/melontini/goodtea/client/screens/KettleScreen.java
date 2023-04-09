package me.melontini.goodtea.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.crackerutil.client.util.DrawUtil;
import me.melontini.crackerutil.util.TextUtil;
import me.melontini.goodtea.screens.KettleScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

import static me.melontini.goodtea.GoodTea.MODID;

public class KettleScreen extends HandledScreen<KettleScreenHandler> {

    private static final Text WATER_LEVEL = TextUtil.translatable("gui.good-tea.water-level.title");
    private final Identifier BACKGROUND_TEXTURE = new Identifier(MODID, "textures/gui/kettle_screen.png");

    public KettleScreen(KettleScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);

        float b = this.handler.getWaterLevel();
        DrawUtil.drawTexture(matrices, i + 7, j + 16 + 54 - b, getZOffset(), 176, 17 + 54 - b, 18, 18 + b);

        float a = this.handler.getTeaProgress();
        DrawUtil.drawTexture(matrices, i + 79, j + 34, getZOffset(), 176, 0, a + 1, 16);

        if ((mouseX <= i + 7 + 18 && mouseX >= i + 7) && (mouseY <= j + 16 + 54 && mouseY >= j + 16)) {
            List<Text> text = List.of(WATER_LEVEL, TextUtil.translatable("gui.good-tea.water-level", this.handler.getWaterLevelUnscaled()).formatted(Formatting.GRAY));
            renderTooltip(matrices, text, mouseX, mouseY);
        }
    }
}
