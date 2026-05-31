package com.luipy.utilsmod.client.inventory;

import com.luipy.utilsmod.inventory.LuipyUnifiedMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/**
 * Client screen for {@link LuipyUnifiedMenu}. Blit coordinates match menu slot positions 1:1.
 */
public class LuipyUnifiedScreen extends EffectRenderingInventoryScreen<LuipyUnifiedMenu> implements RecipeUpdateListener {
	private static final int RECIPE_BOOK_NARROW_WIDTH = 379;
	private static final ResourceLocation GENERIC_54 = new ResourceLocation("textures/gui/container/generic_54.png");
	private static final ResourceLocation CRAFTING_TABLE = new ResourceLocation("textures/gui/container/crafting_table.png");
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");

	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private boolean widthTooNarrow;
	private boolean recipeBookButtonClicked;

	public LuipyUnifiedScreen(LuipyUnifiedMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = menu.playerSectionTop + LuipyUnifiedMenu.PLAYER_PANEL_HEIGHT;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void containerTick() {
		if (this.menu.withCrafting) {
			this.recipeBookComponent.tick();
		}
	}

	@Override
	protected void init() {
		super.init();
		if (!this.menu.withCrafting) {
			return;
		}
		this.widthTooNarrow = this.width < RECIPE_BOOK_NARROW_WIDTH;
		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		this.addRenderableWidget(
			new ImageButton(
				this.leftPos + 5,
				this.recipeBookButtonY(),
				20,
				18,
				0,
				0,
				19,
				RECIPE_BUTTON_TEXTURE,
				btn -> {
					this.recipeBookComponent.toggleVisibility();
					this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
					btn.setPosition(this.leftPos + 5, this.recipeBookButtonY());
					this.recipeBookButtonClicked = true;
				}
			)
		);
		this.addWidget(this.recipeBookComponent);
		this.setInitialFocus(this.recipeBookComponent);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(this.font, this.title, this.titleLabelX, 6, 4210752, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		if (this.menu.withCrafting && this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBg(graphics, partialTick, mouseX, mouseY);
			this.recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
		} else {
			if (this.menu.withCrafting) {
				this.recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
			}
			super.render(graphics, mouseX, mouseY, partialTick);
			if (this.menu.withCrafting) {
				this.recipeBookComponent.renderGhostRecipe(graphics, this.leftPos, this.topPos, false, partialTick);
			}
		}
		this.renderTooltip(graphics, mouseX, mouseY);
		if (this.menu.withCrafting) {
			this.recipeBookComponent.renderTooltip(graphics, this.leftPos, this.topPos, mouseX, mouseY);
		}
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos;

		if (this.menu.withEnder) {
			graphics.blit(GENERIC_54, x, y, 0, 0, this.imageWidth, LuipyUnifiedMenu.ENDER_PANEL_HEIGHT, 256, 256);
		}
		if (this.menu.withCrafting) {
			int craftY = this.menu.withEnder ? LuipyUnifiedMenu.ENDER_PANEL_HEIGHT : 0;
			graphics.blit(CRAFTING_TABLE, x, y + craftY, 0, 0,
				this.imageWidth, LuipyUnifiedMenu.CRAFTING_PANEL_HEIGHT, 256, 256);
		}

		// Compact player section: offhand, main inventory, hotbar (no armor / 2×2 crafting).
		int playerY = y + this.menu.playerSectionTop;
		graphics.blit(
			INVENTORY_LOCATION,
			x,
			playerY,
			0,
			LuipyUnifiedMenu.PLAYER_TEXTURE_SRC_V,
			this.imageWidth,
			LuipyUnifiedMenu.PLAYER_PANEL_HEIGHT,
			256,
			256
		);
	}

	@Override
	protected boolean isHovering(int slotX, int slotY, int width, int height, double mouseX, double mouseY) {
		if (this.menu.withCrafting && this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
			return false;
		}
		return super.isHovering(slotX, slotY, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.menu.withCrafting && this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
			this.setFocused(this.recipeBookComponent);
			return true;
		}
		if (this.menu.withCrafting && this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
			return false;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.recipeBookButtonClicked) {
			this.recipeBookButtonClicked = false;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int button) {
		boolean outsideMain = mouseX < (double) guiLeft
			|| mouseY < (double) guiTop
			|| mouseX >= (double) (guiLeft + this.imageWidth)
			|| mouseY >= (double) (guiTop + this.imageHeight);
		if (!this.menu.withCrafting) {
			return outsideMain;
		}
		return outsideMain
			&& this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, this.imageWidth, this.imageHeight, button);
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
		super.slotClicked(slot, slotId, mouseButton, clickType);
		if (this.menu.withCrafting) {
			this.recipeBookComponent.slotClicked(slot);
		}
	}

	@Override
	public void recipesUpdated() {
		if (this.menu.withCrafting) {
			this.recipeBookComponent.recipesUpdated();
		}
	}

	@Override
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookComponent;
	}

	private int recipeBookButtonY() {
		int craftPanelTop = this.menu.withEnder ? LuipyUnifiedMenu.ENDER_PANEL_HEIGHT : 0;
		return this.topPos + craftPanelTop + 17;
	}
}
