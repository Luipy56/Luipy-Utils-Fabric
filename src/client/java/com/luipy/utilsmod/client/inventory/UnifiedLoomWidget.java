package com.luipy.utilsmod.client.inventory;

import com.luipy.utilsmod.inventory.LuipyUnifiedMenu;
import com.luipy.utilsmod.inventory.workstation.WorkstationKind;
import com.luipy.utilsmod.inventory.workstation.WorkstationPanelHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BannerPattern;

/**
 * Loom pattern list for a panel embedded in {@link LuipyUnifiedScreen}.
 */
final class UnifiedLoomWidget {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/loom.png");
	private static final int PATTERNS_X = 60;
	private static final int PATTERNS_Y = 13;

	private float scrollOffs;
	private boolean scrolling;
	private int startRow;
	private boolean displayPatterns;

	void containerChanged(WorkstationPanelHost.LoomDelegate menu) {
		displayPatterns = menu.getBannerSlot().hasItem()
			&& menu.getDyeSlot().hasItem()
			&& !menu.getSelectablePatterns().isEmpty();
		if (startRow >= totalRowCount(menu) - 4) {
			startRow = 0;
			scrollOffs = 0.0f;
		}
	}

	void render(GuiGraphics graphics, WorkstationPanelHost.LoomDelegate menu, int panelLeft, int panelTop, int mouseX, int mouseY) {
		if (!displayPatterns) {
			return;
		}
		int scrollerY = (int) (41.0f * scrollOffs);
		graphics.blit(TEXTURE, panelLeft + 119, panelTop + 13 + scrollerY, 232, 0, 12, 15);
		int x = panelLeft + PATTERNS_X;
		int y = panelTop + PATTERNS_Y;
		var patterns = menu.getSelectablePatterns();
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				int index = (row + startRow) * 4 + col;
				if (index >= patterns.size()) {
					return;
				}
				int px = x + col * 14;
				int py = y + row * 14;
				boolean hover = mouseX >= px && mouseY >= py && mouseX < px + 14 && mouseY < py + 14;
				int v = 166;
				if (index == menu.getSelectedBannerPatternIndex()) {
					v += 14;
				} else if (hover) {
					v += 28;
				}
				graphics.blit(TEXTURE, px, py, 0, v, 14, 14);
				renderPatternIcon(graphics, patterns.get(index), px + 1, py + 1);
			}
		}
	}

	void renderTooltip(
		GuiGraphics graphics,
		WorkstationPanelHost.LoomDelegate menu,
		Minecraft client,
		int panelLeft,
		int panelTop,
		int mouseX,
		int mouseY
	) {
		if (!displayPatterns) {
			return;
		}
		int x = panelLeft + PATTERNS_X;
		int y = panelTop + PATTERNS_Y;
		var patterns = menu.getSelectablePatterns();
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				int index = (row + startRow) * 4 + col;
				if (index >= patterns.size()) {
					return;
				}
				int px = x + col * 14;
				int py = y + row * 14;
				if (mouseX >= px && mouseY >= py && mouseX < px + 14 && mouseY < py + 14) {
					graphics.renderTooltip(client.font, patternName(patterns.get(index)), mouseX, mouseY);
					return;
				}
			}
		}
	}

	private net.minecraft.network.chat.Component patternName(Holder<BannerPattern> holder) {
		return net.minecraft.network.chat.Component.literal(holder.value().getHashname());
	}

	private void renderPatternIcon(GuiGraphics graphics, Holder<BannerPattern> holder, int x, int y) {
		// Pattern preview uses the slot background; name shows on hover.
	}

	boolean mouseClicked(
		LuipyUnifiedMenu unifiedMenu,
		WorkstationPanelHost.LoomDelegate menu,
		Player player,
		int panelLeft,
		int panelTop,
		double mouseX,
		double mouseY
	) {
		scrolling = false;
		if (!displayPatterns) {
			return false;
		}
		int x = panelLeft + PATTERNS_X;
		int y = panelTop + PATTERNS_Y;
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				double localX = mouseX - (x + col * 14);
				double localY = mouseY - (y + row * 14);
				int index = (row + startRow) * 4 + col;
				if (localX >= 0.0 && localY >= 0.0 && localX < 14.0 && localY < 14.0
					&& index < menu.getSelectablePatterns().size()
					&& unifiedMenu.clickMenuButton(player, index)) {
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0f));
					Minecraft.getInstance().gameMode.handleInventoryButtonClick(unifiedMenu.containerId, index);
					return true;
				}
			}
		}
		int barX = panelLeft + 119;
		int barY = panelTop + 9;
		if (mouseX >= barX && mouseX < barX + 12 && mouseY >= barY && mouseY < barY + 56) {
			scrolling = true;
			return true;
		}
		return false;
	}

	boolean mouseDragged(WorkstationPanelHost.LoomDelegate menu, int panelTop, double mouseY) {
		int extraRows = totalRowCount(menu) - 4;
		if (scrolling && displayPatterns && extraRows > 0) {
			int trackTop = panelTop + PATTERNS_Y;
			int trackBottom = trackTop + 56;
			scrollOffs = Mth.clamp((float) (mouseY - trackTop - 7.5f) / (float) (trackBottom - trackTop - 15.0f), 0.0f, 1.0f);
			startRow = Math.max((int) (scrollOffs * extraRows + 0.5), 0);
			return true;
		}
		return false;
	}

	boolean mouseScrolled(WorkstationPanelHost.LoomDelegate menu, double delta) {
		int extraRows = totalRowCount(menu) - 4;
		if (displayPatterns && extraRows > 0) {
			float step = (float) delta / extraRows;
			scrollOffs = Mth.clamp(scrollOffs - step, 0.0f, 1.0f);
			startRow = Math.max((int) (scrollOffs * extraRows + 0.5f), 0);
			return true;
		}
		return false;
	}

	private int totalRowCount(WorkstationPanelHost.LoomDelegate menu) {
		return Mth.positiveCeilDiv(menu.getSelectablePatterns().size(), 4);
	}
}
