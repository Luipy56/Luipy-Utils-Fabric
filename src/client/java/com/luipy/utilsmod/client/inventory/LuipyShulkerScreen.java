package com.luipy.utilsmod.client.inventory;

import com.luipy.utilsmod.inventory.LuipyShulkerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class LuipyShulkerScreen extends AbstractContainerScreen<LuipyShulkerMenu> {
	public LuipyShulkerScreen(LuipyShulkerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		++this.imageHeight;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.blit(resolveTexture(), x, y, 0, 0, this.imageWidth, this.imageHeight);
	}

	private ResourceLocation resolveTexture() {
		int slotIdx = this.menu.getPlayerSlotIndex();
		if (slotIdx >= 0 && this.minecraft != null && this.minecraft.player != null) {
			ItemStack item = this.minecraft.player.getInventory().getItem(slotIdx);
			if (!item.isEmpty() && net.minecraft.world.level.block.Block.byItem(item.getItem()) instanceof ShulkerBoxBlock shulker) {
				var color = shulker.getColor();
				if (color != null) {
					return new ResourceLocation("textures/gui/container/shulker/shulker_" + color.getName() + ".png");
				}
			}
		}
		return new ResourceLocation("textures/gui/container/shulker_box.png");
	}
}
