package com.luipy.utilsmod.client.inventory;

import com.luipy.utilsmod.inventory.LuipyInventoryWithEnderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class LuipyInventoryWithEnderScreen extends EffectRenderingInventoryScreen<LuipyInventoryWithEnderMenu> implements RecipeUpdateListener {
	/** Same threshold as {@link InventoryScreen} for recipe-book-only layout. */
	private static final int RECIPE_BOOK_NARROW_WIDTH = 379;
	private static final ResourceLocation GENERIC_54 = new ResourceLocation("textures/gui/container/generic_54.png");
	private static final ResourceLocation CRAFTING_TABLE = new ResourceLocation("textures/gui/container/crafting_table.png");
	/** 1.20.1 recipe-book toggle atlas (pre-{@code WidgetSprites} API). */
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");

	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private boolean widthTooNarrow;
	private boolean recipeBookButtonClicked;
	private float xMouse;
	private float yMouse;

	public LuipyInventoryWithEnderScreen(LuipyInventoryWithEnderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = menu.invYShift + 166;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void containerTick() {
		this.recipeBookComponent.tick();
	}

	@Override
	protected void init() {
		super.init();
		this.widthTooNarrow = this.width < RECIPE_BOOK_NARROW_WIDTH;
		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		this.addRenderableWidget(
			new ImageButton(
				this.leftPos + 104,
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
					btn.setPosition(this.leftPos + 104, this.recipeBookButtonY());
					this.recipeBookButtonClicked = true;
				}
			)
		);
		this.addWidget(this.recipeBookComponent);
		this.setInitialFocus(this.recipeBookComponent);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		int titleY = 6;
		graphics.drawString(this.font, this.title, this.titleLabelX, titleY, 4210752, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBackground(graphics);
			this.recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
		} else {
			super.render(graphics, mouseX, mouseY, partialTick);
			this.recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
			this.recipeBookComponent.renderGhostRecipe(graphics, this.leftPos, this.topPos, false, partialTick);
		}
		this.renderTooltip(graphics, mouseX, mouseY);
		this.recipeBookComponent.renderTooltip(graphics, this.leftPos, this.topPos, mouseX, mouseY);
		this.xMouse = (float) mouseX;
		this.yMouse = (float) mouseY;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos;
		int enderH = LuipyInventoryWithEnderMenu.TOP_PANEL_HEIGHT;
		int invShift = this.menu.invYShift;

		// Ender panel (top)
		graphics.blit(GENERIC_54, x, y, 0, 0, this.imageWidth, enderH, 256, 256);

		// Optional crafting table panel (middle)
		if (this.menu.withCraftingTable) {
			graphics.blit(CRAFTING_TABLE, x, y + enderH, 0, 0,
				this.imageWidth, LuipyInventoryWithEnderMenu.CRAFTING_TABLE_PANEL_HEIGHT, 256, 256);
		}

		// Player inventory (bottom)
		graphics.blit(INVENTORY_LOCATION, x, y + invShift, 0, 0, this.imageWidth, 166, 256, 256);

		if (this.minecraft != null && this.minecraft.player != null) {
			int invTop = y + invShift;
			InventoryScreen.renderEntityInInventoryFollowsMouse(
				graphics,
				x + 51,
				invTop + 75,
				30,
				(float) (x + 51) - this.xMouse,
				(float) (invTop + 75 - 50) - this.yMouse,
				this.minecraft.player
			);
		}
	}

	@Override
	protected boolean isHovering(int slotX, int slotY, int width, int height, double mouseX, double mouseY) {
		if (this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
			return false;
		}
		return super.isHovering(slotX, slotY, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
			this.setFocused(this.recipeBookComponent);
			return true;
		}
		if (this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
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
		return outsideMain
			&& this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, this.imageWidth, this.imageHeight, button);
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
		super.slotClicked(slot, slotId, mouseButton, clickType);
		this.recipeBookComponent.slotClicked(slot);
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookComponent.recipesUpdated();
	}

	@Override
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookComponent;
	}

	/**
	 * Vanilla uses {@code height/2 - 22}; the survival+crafting band is shifted down by half the extra
	 * height above it, so the toggle is moved by the same amount.
	 */
	private int recipeBookButtonY() {
		return this.height / 2 - 22 + this.menu.invYShift / 2;
	}
}
