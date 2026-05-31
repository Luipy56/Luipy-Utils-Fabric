package com.luipy.utilsmod.inventory;

import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Luipy unified menu (Alt+L): optional ender chest and crafting table panels above a compact player
 * inventory (main 3×9, hotbar, offhand — no armor column, no 2×2 player crafting).
 *
 * <p>Slot indices are assigned top-to-bottom at construction time. Example when both optional panels
 * are enabled ({@code E=27} ender slots, {@code C=10} crafting slots = 9 grid + 1 result):
 * <pre>
 *   [0 .. E-1]           → player ender chest inventory
 *   [E .. E+C-2]         → transient 3×3 crafting grid
 *   [E+C-1]              → crafting result
 *   [E+C .. E+C+26]      → player main inventory (backing indices 9–35)
 *   [E+C+27 .. E+C+35]   → player hotbar (backing indices 0–8)
 *   [E+C+36]             → player offhand (backing index 40)
 * </pre>
 *
 * <p>Layout map (screen Y from top; must match {@code LuipyUnifiedScreen.renderBg} blits 1:1):
 * <pre>
 *   Ender (optional):     slots at y = 18 + row×18; blit generic_54 (0,0) height {@link #ENDER_PANEL_HEIGHT}
 *   Craft (optional):     grid y = 17 + row×18 + craftPanelTop; result y = 35 + craftPanelTop;
 *                         blit crafting_table at craftPanelTop, height {@link #CRAFTING_PANEL_HEIGHT}
 *   Player section:       blit inventory.png srcV={@link #PLAYER_TEXTURE_SRC_V}, height {@link #PLAYER_PANEL_HEIGHT}
 *     offhand y = ps + {@link #PLAYER_OFFHAND_Y}   (vanilla 62 − srcV)
 *     main y    = ps + {@link #PLAYER_MAIN_Y} + row×18 (vanilla 84 − srcV)
 *     hotbar y  = ps + {@link #PLAYER_HOTBAR_Y}   (vanilla 142 − srcV)
 *   where ps = {@link #playerSectionTop}.
 * </pre>
 *
 * <p>When a panel is disabled its slots are omitted and downstream indices shift accordingly.
 * See instance fields {@link #enderStart}, {@link #craftGridStart}, etc. for the live map.
 *
 * <p>Extension point: add future panels by inserting new slot blocks before the player section and
 * updating {@link #quickMoveStack} ranges in one place.
 */
public class LuipyUnifiedMenu extends RecipeBookMenu<CraftingContainer> {
	public static final int ENDER_SLOT_COUNT = 27;
	public static final int CRAFT_GRID_COUNT = 9;
	/** Vertical space for the ender chest panel background (vanilla generic_54 header + 3 rows). */
	public static final int ENDER_PANEL_HEIGHT = 17 + 3 * 18;
	/** Vertical space for the crafting table panel background. */
	public static final int CRAFTING_PANEL_HEIGHT = 17 + 3 * 18;
	/** inventory.png srcV for the compact player strip (excludes armor / 2×2 craft chrome). */
	public static final int PLAYER_TEXTURE_SRC_V = 51;
	/** Vertical space for the compact player section (matches blitted inventory.png height). */
	public static final int PLAYER_PANEL_HEIGHT = 115;
	/** Y offset from player-section top to the offhand slot (vanilla 62 − {@link #PLAYER_TEXTURE_SRC_V}). */
	public static final int PLAYER_OFFHAND_Y = 11;
	/** Y offset from player-section top to the first main-inventory row (vanilla 84 − srcV). */
	public static final int PLAYER_MAIN_Y = 33;
	/** Y offset from player-section top to the hotbar row (vanilla 142 − srcV). */
	public static final int PLAYER_HOTBAR_Y = 91;

	public final boolean withEnder;
	public final boolean withCrafting;
	/** Y offset from screen top to the player inventory section. */
	public final int playerSectionTop;

	/** Inclusive start of ender chest menu slots, or {@code -1} when {@link #withEnder} is false. */
	public final int enderStart;
	/** Exclusive end of ender chest menu slots, or {@code -1}. */
	public final int enderEnd;
	/** First menu slot of the 3×3 crafting grid, or {@code -1}. */
	public final int craftGridStart;
	/** Menu slot of the crafting result, or {@code -1}. */
	public final int craftResultIndex;
	/** First menu slot of main inventory (player backing 9–35). */
	public final int mainStart;
	/** First menu slot of hotbar (player backing 0–8). */
	public final int hotbarStart;
	/** Menu slot of offhand (player backing 40). */
	public final int offhandIndex;
	/** Exclusive end of all player-backed slots (offhand + 1). */
	public final int playerEndExclusive;

	private final CraftingContainer craftGrid;
	private final ResultContainer craftResult;
	private final Player owner;
	private final Container enderChest;

	public LuipyUnifiedMenu(int syncId, Inventory inventory) {
		super(LuipyMenuTypes.UNIFIED, syncId);
		var cfg = LuipyUtilsConfigManager.get();
		this.withEnder = cfg.showEnderChestWithInventory;
		this.withCrafting = cfg.showCraftingTableWithInventory;
		this.playerSectionTop = (withEnder ? ENDER_PANEL_HEIGHT : 0) + (withCrafting ? CRAFTING_PANEL_HEIGHT : 0);

		this.craftGrid = new TransientCraftingContainer(this, 3, 3);
		this.craftResult = new ResultContainer();
		this.owner = inventory.player;
		this.enderChest = inventory.player.getEnderChestInventory();

		if (withEnder) {
			enderChest.startOpen(inventory.player);
		}

		int idx = 0;
		int es = -1;
		int ee = -1;
		int cgs = -1;
		int cri = -1;

		if (withEnder) {
			es = idx;
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 9; col++) {
					addSlot(new Slot(enderChest, col + row * 9, 8 + col * 18, 18 + row * 18));
					idx++;
				}
			}
			ee = idx;
		}

		if (withCrafting) {
			int tableY = withEnder ? ENDER_PANEL_HEIGHT : 0;
			cgs = idx;
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					addSlot(new Slot(craftGrid, col + row * 3, 30 + col * 18, 17 + row * 18 + tableY));
					idx++;
				}
			}
			cri = idx;
			addSlot(new ResultSlot(inventory.player, craftGrid, craftResult, 0, 124, 35 + tableY));
			idx++;
		}

		this.enderStart = es;
		this.enderEnd = ee;
		this.craftGridStart = cgs;
		this.craftResultIndex = cri;

		int ps = playerSectionTop;
		this.mainStart = idx;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + (row + 1) * 9, 8 + col * 18, ps + PLAYER_MAIN_Y + row * 18));
				idx++;
			}
		}
		this.hotbarStart = idx;
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, ps + PLAYER_HOTBAR_Y));
			idx++;
		}
		this.offhandIndex = idx;
		addSlot(new Slot(inventory, 40, 77, ps + PLAYER_OFFHAND_Y) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});
		idx++;
		this.playerEndExclusive = idx;
	}

	@Override
	public void slotsChanged(Container container) {
		if (withCrafting && container == craftGrid) {
			CraftingMenu.slotChangedCraftingGrid(this, owner.level(), owner, craftGrid, craftResult);
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (withCrafting) {
			craftResult.clearContent();
			if (!player.level().isClientSide()) {
				clearContainer(player, craftGrid);
			}
		}
		if (withEnder) {
			enderChest.stopOpen(player);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	/**
	 * Shift-click routing: ender/craft → player; player main ↔ hotbar; player → ender/craft when room.
	 * Ranges use the instance slot map so panel toggles never leave stale index math.
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack ret = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack moved = slot.getItem();
		ret = moved.copy();

		if (withEnder && index >= enderStart && index < enderEnd) {
			if (!moveToPlayerMainAndHotbar(moved, false)) {
				return ItemStack.EMPTY;
			}
		} else if (withCrafting && index == craftResultIndex) {
			if (!moveToPlayerMainAndHotbar(moved, false)) {
				return ItemStack.EMPTY;
			}
		} else if (withCrafting && index >= craftGridStart && index < craftResultIndex) {
			if (!moveToPlayerMainAndHotbar(moved, false)) {
				return ItemStack.EMPTY;
			}
		} else if (index >= mainStart && index < hotbarStart) {
			// Main inventory → hotbar first, then optional panels.
			if (!moveItemStackTo(moved, hotbarStart, offhandIndex, false)) {
				if (!moveToOptionalPanels(moved)) {
					return ItemStack.EMPTY;
				}
			}
		} else if (index >= hotbarStart && index < offhandIndex) {
			// Hotbar → main inventory first, then optional panels.
			if (!moveItemStackTo(moved, mainStart, hotbarStart, false)) {
				if (!moveToOptionalPanels(moved)) {
					return ItemStack.EMPTY;
				}
			}
		} else if (index == offhandIndex) {
			if (!moveItemStackTo(moved, mainStart, offhandIndex, false)) {
				if (!moveToOptionalPanels(moved)) {
					return ItemStack.EMPTY;
				}
			}
		} else {
			return ItemStack.EMPTY;
		}

		if (moved.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		if (moved.getCount() == ret.getCount()) {
			return ItemStack.EMPTY;
		}
		slot.onTake(player, moved);
		return ret;
	}

	/**
	 * Shift-click destination for optional panels → player inventory.
	 * Main inventory first, then hotbar — never offhand (same order as vanilla chest/ender menus).
	 */
	private boolean moveToPlayerMainAndHotbar(ItemStack moved, boolean reverse) {
		if (moveItemStackTo(moved, mainStart, hotbarStart, reverse)) {
			return true;
		}
		return moveItemStackTo(moved, hotbarStart, offhandIndex, reverse);
	}

	/** Tries ender chest then crafting grid for player-initiated shift-clicks. */
	private boolean moveToOptionalPanels(ItemStack moved) {
		if (withEnder && moveItemStackTo(moved, enderStart, enderEnd, false)) {
			return true;
		}
		return withCrafting && moveItemStackTo(moved, craftGridStart, craftResultIndex, false);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return !withCrafting || slot.container != craftResult;
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents finder) {
		if (withCrafting) {
			craftGrid.fillStackedContents(finder);
		}
	}

	@Override
	public void clearCraftingContent() {
		if (withCrafting) {
			craftResult.clearContent();
			craftGrid.clearContent();
		}
	}

	@Override
	public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
		return withCrafting && recipe.matches(craftGrid, owner.level());
	}

	@Override
	public int getResultSlotIndex() {
		return withCrafting ? craftResultIndex : 0;
	}

	@Override
	public int getGridWidth() {
		return withCrafting ? craftGrid.getWidth() : 1;
	}

	@Override
	public int getGridHeight() {
		return withCrafting ? craftGrid.getHeight() : 1;
	}

	@Override
	public int getSize() {
		return withCrafting ? CRAFT_GRID_COUNT + 1 : 0;
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int slotIndex) {
		return !withCrafting || slotIndex != craftResultIndex;
	}
}
