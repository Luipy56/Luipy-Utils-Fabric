package com.luipy.utilsmod.client.inventory;

import com.luipy.utilsmod.inventory.LuipyUnifiedMenu;
import com.luipy.utilsmod.inventory.workstation.UnifiedWorkstationLayout;
import com.luipy.utilsmod.inventory.workstation.WorkstationKind;
import com.luipy.utilsmod.inventory.workstation.WorkstationPanelHost;
import java.util.EnumMap;
import java.util.Map;
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
 * Client screen for {@link LuipyUnifiedMenu}. Left column = workstation panels; right = ender/craft/player.
 */
public class LuipyUnifiedScreen extends EffectRenderingInventoryScreen<LuipyUnifiedMenu> implements RecipeUpdateListener {
	private static final int RECIPE_BOOK_NARROW_WIDTH = 379 + UnifiedWorkstationLayout.LEFT_COLUMN_WIDTH;
	private static final int MAX_VISIBLE_HEIGHT = 240;

	private static final ResourceLocation GENERIC_54 = new ResourceLocation("textures/gui/container/generic_54.png");
	private static final ResourceLocation CRAFTING_TABLE = new ResourceLocation("textures/gui/container/crafting_table.png");
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
	private static final ResourceLocation ANVIL = new ResourceLocation("textures/gui/container/anvil.png");
	private static final ResourceLocation SMITHING = new ResourceLocation("textures/gui/container/smithing.png");
	private static final ResourceLocation CARTOGRAPHY = new ResourceLocation("textures/gui/container/cartography_table.png");
	private static final ResourceLocation GRINDSTONE = new ResourceLocation("textures/gui/container/grindstone.png");
	private static final ResourceLocation STONECUTTER = new ResourceLocation("textures/gui/container/stonecutter.png");
	private static final ResourceLocation LOOM = new ResourceLocation("textures/gui/container/loom.png");

	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private final Map<WorkstationKind, UnifiedStonecutterWidget> stonecutterWidgets = new EnumMap<>(WorkstationKind.class);
	private final Map<WorkstationKind, UnifiedLoomWidget> loomWidgets = new EnumMap<>(WorkstationKind.class);
	private boolean widthTooNarrow;
	private boolean recipeBookButtonClicked;
	private double scrollOffset;

	public LuipyUnifiedScreen(LuipyUnifiedMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = this.menu.rightColumnX + 176;
		this.imageHeight = menu.totalContentHeight;
		this.inventoryLabelY = this.imageHeight - 94;
		if (menu.workstationHost.stonecutter() != null) {
			var widget = new UnifiedStonecutterWidget();
			menu.workstationHost.stonecutter().registerUpdateListener(() -> widget.containerChanged(menu.workstationHost.stonecutter()));
			stonecutterWidgets.put(WorkstationKind.STONECUTTER, widget);
		}
		if (menu.workstationHost.loom() != null) {
			var widget = new UnifiedLoomWidget();
			menu.workstationHost.loom().registerUpdateListener(() -> widget.containerChanged(menu.workstationHost.loom()));
			loomWidgets.put(WorkstationKind.LOOM, widget);
		}
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
		clampScroll();
		if (!this.menu.withCrafting) {
			return;
		}
		this.widthTooNarrow = this.width < RECIPE_BOOK_NARROW_WIDTH;
		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		this.addRenderableWidget(
			new ImageButton(
				this.leftPos + this.menu.rightColumnX + 5,
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
					btn.setPosition(this.leftPos + this.menu.rightColumnX + 5, this.recipeBookButtonY());
					this.recipeBookButtonClicked = true;
				}
			)
		);
		this.addWidget(this.recipeBookComponent);
		this.setInitialFocus(this.recipeBookComponent);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(this.font, this.title, this.titleLabelX, (int) (6 - scrollOffset), 4210752, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		double adjustedMouseY = mouseY + scrollOffset;
		if (this.menu.withCrafting && this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBg(graphics, partialTick, mouseX, (int) adjustedMouseY);
			this.recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
		} else {
			if (this.menu.withCrafting) {
				this.recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
			}
			super.render(graphics, mouseX, (int) adjustedMouseY, partialTick);
			if (this.menu.withCrafting) {
				this.recipeBookComponent.renderGhostRecipe(graphics, this.leftPos, this.topPos, false, partialTick);
			}
		}
		renderWorkstationOverlays(graphics, mouseX, (int) adjustedMouseY);
		this.renderTooltip(graphics, mouseX, (int) adjustedMouseY);
		if (this.menu.withCrafting) {
			this.recipeBookComponent.renderTooltip(graphics, this.leftPos, this.topPos, mouseX, mouseY);
		}
		renderWorkstationTooltips(graphics, mouseX, (int) adjustedMouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos - (int) scrollOffset;

		for (WorkstationKind kind : this.menu.enabledWorkstations) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(kind);
			if (range == null) {
				continue;
			}
			int panelY = y + range.panelTop();
			graphics.blit(workstationTexture(kind), x, panelY, 0, 0, 176, kind.panelHeight, 256, 256);
		}

		if (this.menu.withEnder) {
			graphics.blit(
				GENERIC_54,
				x + this.menu.rightColumnX,
				y + this.menu.rightColumnContentTop,
				0,
				0,
				176,
				LuipyUnifiedMenu.ENDER_PANEL_HEIGHT,
				256,
				256
			);
		}
		if (this.menu.withCrafting) {
			int craftY = this.menu.rightColumnContentTop + (this.menu.withEnder ? LuipyUnifiedMenu.ENDER_PANEL_HEIGHT : 0);
			graphics.blit(
				CRAFTING_TABLE,
				x + this.menu.rightColumnX,
				y + craftY,
				0,
				0,
				176,
				LuipyUnifiedMenu.CRAFTING_PANEL_HEIGHT,
				256,
				256
			);
		}

		int playerY = y + this.menu.playerSectionTop;
		graphics.blit(
			INVENTORY_LOCATION,
			x + this.menu.rightColumnX,
			playerY,
			0,
			LuipyUnifiedMenu.PLAYER_TEXTURE_SRC_V,
			176,
			LuipyUnifiedMenu.PLAYER_PANEL_HEIGHT,
			256,
			256
		);
	}

	private ResourceLocation workstationTexture(WorkstationKind kind) {
		return switch (kind) {
			case ANVIL -> ANVIL;
			case SMITHING -> SMITHING;
			case CARTOGRAPHY -> CARTOGRAPHY;
			case GRINDSTONE -> GRINDSTONE;
			case STONECUTTER -> STONECUTTER;
			case LOOM -> LOOM;
		};
	}

	private void renderWorkstationOverlays(GuiGraphics graphics, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos - (int) scrollOffset;
		if (this.menu.workstationHost.stonecutter() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.STONECUTTER);
			if (range != null) {
				stonecutterWidgets.get(WorkstationKind.STONECUTTER)
					.render(graphics, this.menu.workstationHost.stonecutter(), x, y + range.panelTop(), mouseX, mouseY);
			}
		}
		if (this.menu.workstationHost.loom() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.LOOM);
			if (range != null) {
				loomWidgets.get(WorkstationKind.LOOM)
					.render(graphics, this.menu.workstationHost.loom(), x, y + range.panelTop(), mouseX, mouseY);
			}
		}
	}

	private void renderWorkstationTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos - (int) scrollOffset;
		if (this.menu.workstationHost.stonecutter() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.STONECUTTER);
			if (range != null) {
				stonecutterWidgets.get(WorkstationKind.STONECUTTER)
					.renderTooltip(graphics, this.menu.workstationHost.stonecutter(), this.minecraft, x, y + range.panelTop(), mouseX, mouseY);
			}
		}
		if (this.menu.workstationHost.loom() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.LOOM);
			if (range != null) {
				loomWidgets.get(WorkstationKind.LOOM)
					.renderTooltip(graphics, this.menu.workstationHost.loom(), this.minecraft, x, y + range.panelTop(), mouseX, mouseY);
			}
		}
	}

	@Override
	protected boolean isHovering(int slotX, int slotY, int width, int height, double mouseX, double mouseY) {
		double adjustedMouseY = mouseY + scrollOffset;
		if (this.menu.withCrafting && this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
			return false;
		}
		return super.isHovering(slotX, slotY, width, height, mouseX, adjustedMouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		double adjustedMouseY = mouseY + scrollOffset;
		if (handleWorkstationMouseClicked(mouseX, adjustedMouseY)) {
			return true;
		}
		if (this.menu.withCrafting && this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
			this.setFocused(this.recipeBookComponent);
			return true;
		}
		if (this.menu.withCrafting && this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
			return false;
		}
		return super.mouseClicked(mouseX, adjustedMouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		double adjustedMouseY = mouseY + scrollOffset;
		if (handleWorkstationMouseDragged(adjustedMouseY)) {
			return true;
		}
		return super.mouseDragged(mouseX, adjustedMouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (handleWorkstationMouseScrolled(delta)) {
			return true;
		}
		if (maxScroll() > 0) {
			scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset - delta * 16));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	private boolean handleWorkstationMouseClicked(double mouseX, double mouseY) {
		int x = this.leftPos;
		int y = this.topPos;
		if (this.menu.workstationHost.stonecutter() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.STONECUTTER);
			if (range != null && stonecutterWidgets.get(WorkstationKind.STONECUTTER)
				.mouseClicked(this.menu, this.menu.workstationHost.stonecutter(), this.minecraft.player, x, y + range.panelTop(), mouseX, mouseY)) {
				return true;
			}
		}
		if (this.menu.workstationHost.loom() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.LOOM);
			if (range != null && loomWidgets.get(WorkstationKind.LOOM)
				.mouseClicked(this.menu, this.menu.workstationHost.loom(), this.minecraft.player, x, y + range.panelTop(), mouseX, mouseY)) {
				return true;
			}
		}
		return false;
	}

	private boolean handleWorkstationMouseDragged(double mouseY) {
		int y = this.topPos;
		if (this.menu.workstationHost.stonecutter() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.STONECUTTER);
			if (range != null && stonecutterWidgets.get(WorkstationKind.STONECUTTER)
				.mouseDragged(this.menu.workstationHost.stonecutter(), y + range.panelTop(), mouseY)) {
				return true;
			}
		}
		if (this.menu.workstationHost.loom() != null) {
			WorkstationPanelHost.SlotRange range = this.menu.workstationHost.slotRanges().get(WorkstationKind.LOOM);
			if (range != null && loomWidgets.get(WorkstationKind.LOOM)
				.mouseDragged(this.menu.workstationHost.loom(), y + range.panelTop(), mouseY)) {
				return true;
			}
		}
		return false;
	}

	private boolean handleWorkstationMouseScrolled(double delta) {
		if (this.menu.workstationHost.stonecutter() != null
			&& stonecutterWidgets.get(WorkstationKind.STONECUTTER).mouseScrolled(this.menu.workstationHost.stonecutter(), delta)) {
			return true;
		}
		return this.menu.workstationHost.loom() != null
			&& loomWidgets.get(WorkstationKind.LOOM).mouseScrolled(this.menu.workstationHost.loom(), delta);
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
		double adjustedMouseY = mouseY + scrollOffset;
		boolean outsideMain = mouseX < (double) guiLeft
			|| adjustedMouseY < (double) guiTop
			|| mouseX >= (double) (guiLeft + this.imageWidth)
			|| adjustedMouseY >= (double) (guiTop + this.imageHeight);
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
		int craftPanelTop = this.menu.rightColumnContentTop
			+ (this.menu.withEnder ? LuipyUnifiedMenu.ENDER_PANEL_HEIGHT : 0);
		return this.topPos + craftPanelTop + 17 - (int) scrollOffset;
	}

	private double maxScroll() {
		return Math.max(0, this.imageHeight - MAX_VISIBLE_HEIGHT);
	}

	private void clampScroll() {
		scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset));
	}

	public double luipyScrollOffset() {
		return scrollOffset;
	}
}
