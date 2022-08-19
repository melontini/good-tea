package me.melontini.goodtea.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.goodtea.screens.KettleScreenHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static me.melontini.goodtea.GoodTea.MODID;

public class KettleScreen extends HandledScreen<KettleScreenHandler> {
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int b = this.handler.getWaterLevel();
        this.drawTexture(matrices, i + 7, j + 16 + 54 - b, 176, 17 + 54 - b, 18, 18 + b);

        int a = this.handler.getTeaProgress();
        this.drawTexture(matrices, i + 79, j + 34, 176, 0, a + 1, 16);
    }

    @Override
    protected void drawForeground(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.drawForeground(matrixStack, mouseX, mouseY);
        itemRenderer.renderInGui(new ItemStack(Blocks.WATER), mouseX, mouseY);
    }
}
