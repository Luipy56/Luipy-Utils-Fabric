package com.luipy.utilsmod.client.inventory;

import com.luipy.utilsmod.inventory.LuipyUnifiedMenu;
import com.luipy.utilsmod.inventory.workstation.WorkstationKind;
import com.luipy.utilsmod.inventory.workstation.WorkstationPanelHost;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.StonecutterRecipe;

/**
 * Stonecutter recipe list for a panel embedded in {@link LuipyUnifiedScreen}.
 */
final class UnifiedStonecutterWidget {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/stonecutter.png");
	private static final int RECIPES_X = 52;
	private static final int RECIPES_Y = 14;

	private float scrollOffs;
	private boolean scrolling;
	private int startIndex;
	private boolean displayRecipes;

	void containerChanged(WorkstationPanelHost.StonecutterDelegate menu) {
		this.displayRecipes = menu.hasInputItem();
		if (!this.displayRecipes) {
			this.scrollOffs = 0.0f;
			this.startIndex = 0;
		}
	}

	void render(GuiGraphics graphics, WorkstationPanelHost.StonecutterDelegate menu, int panelLeft, int panelTop, int mouseX, int mouseY) {
		if (!displayRecipes) {
			return;
		}
		int scrollerY = (int) (41.0f * scrollOffs);
		graphics.blit(TEXTURE, panelLeft + 119, panelTop + 15 + scrollerY, 176 + (isScrollBarActive(menu) ? 0 : 12), 0, 12, 15);
		int recipeX = panelLeft + RECIPES_X;
		int recipeY = panelTop + RECIPES_Y;
		int end = startIndex + 12;
		renderButtons(graphics, menu, mouseX, mouseY, recipeX, recipeY, end);
		renderRecipes(graphics, menu, recipeX, recipeY, end);
	}

	void renderTooltip(
		GuiGraphics graphics,
		WorkstationPanelHost.StonecutterDelegate menu,
		Minecraft client,
		int panelLeft,
		int panelTop,
		int mouseX,
		int mouseY
	) {
		if (!displayRecipes) {
			return;
		}
		int recipeX = panelLeft + RECIPES_X;
		int recipeY = panelTop + RECIPES_Y;
		int end = startIndex + 12;
		var recipes = menu.getRecipes();
		for (int i = startIndex; i < end && i < menu.getNumRecipes(); i++) {
			int col = i - startIndex;
			int x = recipeX + col % 4 * 16;
			int y = recipeY + col / 4 * 18 + 2;
			if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 18) {
				graphics.renderTooltip(client.font, recipes.get(i).getResultItem(client.level.registryAccess()), mouseX, mouseY);
				return;
			}
		}
	}

	boolean mouseClicked(
		LuipyUnifiedMenu unifiedMenu,
		WorkstationPanelHost.StonecutterDelegate menu,
		Player player,
		int panelLeft,
		int panelTop,
		double mouseX,
		double mouseY
	) {
		scrolling = false;
		if (!displayRecipes) {
			return false;
		}
		int recipeX = panelLeft + RECIPES_X;
		int recipeY = panelTop + RECIPES_Y;
		int end = startIndex + 12;
		for (int i = startIndex; i < end; i++) {
			int col = i - startIndex;
			double localX = mouseX - (recipeX + col % 4 * 16);
			double localY = mouseY - (recipeY + col / 4 * 18);
			if (localX >= 0.0 && localY >= 0.0 && localX < 16.0 && localY < 18.0
				&& unifiedMenu.clickMenuButton(player, i)) {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0f));
				Minecraft.getInstance().gameMode.handleInventoryButtonClick(unifiedMenu.containerId, i);
				return true;
			}
		}
		int barX = panelLeft + 119;
		int barY = panelTop + 9;
		if (mouseX >= barX && mouseX < barX + 12 && mouseY >= barY && mouseY < barY + 54) {
			scrolling = true;
			return true;
		}
		return false;
	}

	boolean mouseDragged(WorkstationPanelHost.StonecutterDelegate menu, int panelTop, double mouseY) {
		if (scrolling && isScrollBarActive(menu)) {
			int trackTop = panelTop + RECIPES_Y;
			int trackBottom = trackTop + 54;
			scrollOffs = Mth.clamp((float) (mouseY - trackTop - 7.5f) / (float) (trackBottom - trackTop - 15.0f), 0.0f, 1.0f);
			startIndex = (int) (scrollOffs * getOffscreenRows(menu) + 0.5) * 4;
			return true;
		}
		return false;
	}

	boolean mouseScrolled(WorkstationPanelHost.StonecutterDelegate menu, double delta) {
		if (isScrollBarActive(menu)) {
			int rows = getOffscreenRows(menu);
			scrollOffs = Mth.clamp(scrollOffs - (float) delta / rows, 0.0f, 1.0f);
			startIndex = (int) (scrollOffs * rows + 0.5) * 4;
			return true;
		}
		return false;
	}

	private void renderButtons(
		GuiGraphics graphics,
		WorkstationPanelHost.StonecutterDelegate menu,
		int mouseX,
		int mouseY,
		int x,
		int y,
		int end
	) {
		for (int i = startIndex; i < end && i < menu.getNumRecipes(); i++) {
			int col = i - startIndex;
			int bx = x + col % 4 * 16;
			int by = y + col / 4 * 18 + 2;
			int v = 166;
			if (i == menu.getSelectedRecipeIndex()) {
				v += 18;
			} else if (mouseX >= bx && mouseY >= by && mouseX < bx + 16 && mouseY < by + 18) {
				v += 36;
			}
			graphics.blit(TEXTURE, bx, by - 1, 0, v, 16, 18);
		}
	}

	private void renderRecipes(GuiGraphics graphics, WorkstationPanelHost.StonecutterDelegate menu, int x, int y, int end) {
		var recipes = menu.getRecipes();
		Minecraft client = Minecraft.getInstance();
		for (int i = startIndex; i < end && i < menu.getNumRecipes(); i++) {
			int col = i - startIndex;
			int rx = x + col % 4 * 16;
			int ry = y + col / 4 * 18 + 2;
			graphics.renderItem(recipes.get(i).getResultItem(client.level.registryAccess()), rx, ry);
		}
	}

	private boolean isScrollBarActive(WorkstationPanelHost.StonecutterDelegate menu) {
		return displayRecipes && menu.getNumRecipes() > 12;
	}

	private int getOffscreenRows(WorkstationPanelHost.StonecutterDelegate menu) {
		return (menu.getNumRecipes() + 4 - 1) / 4 - 3;
	}
}
