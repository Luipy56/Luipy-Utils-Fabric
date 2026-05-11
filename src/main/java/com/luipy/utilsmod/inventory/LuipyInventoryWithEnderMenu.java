package com.luipy.utilsmod.inventory;

import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Survival inventory (crafting, armor, inv, hotbar, offhand) plus 3×9 ender chest rows on top,
 * and optionally a 3×3 crafting table panel between ender and player inventory.
 *
 * <p>Slot layout when crafting table is <strong>disabled</strong> (O = ENDER_SLOTS = 27):
 * <pre>0–4 crafting  |  5–31 ender  |  32–35 armor  |  36–62 main  |  63–71 hotbar  |  72 offhand</pre>
 *
 * <p>Slot layout when crafting table is <strong>enabled</strong> (O = ENDER_SLOTS + CRAFTING_TABLE_TOTAL = 37):
 * <pre>0–4 crafting  |  5–31 ender  |  32–40 table 3×3  |  41 table result
 *   |  42–45 armor  |  46–72 main  |  73–81 hotbar  |  82 offhand</pre>
 */
public class LuipyInventoryWithEnderMenu extends RecipeBookMenu<CraftingContainer> {
	public static final int ENDER_SLOTS = 27;
	/**
	 * Result slot + 2×2 crafting grid must occupy indices {@code 0..4}, matching {@link InventoryMenu}.
	 * The recipe book ghost overlay and {@link net.minecraft.recipebook.PlaceRecipe} assume that layout
	 * on the full {@link #slots} list (see client {@code ClientPacketListener} ghost recipe handling).
	 */
	private static final int CRAFTING_SLOT_COUNT = 1 + 4;
	/** First menu slot index of the 3×9 ender chest panel (immediately after crafting slots). */
	public static final int ENDER_SLOT_START = CRAFTING_SLOT_COUNT;
	/** Exclusive end index of ender chest slots. */
	public static final int ENDER_SLOT_END = ENDER_SLOT_START + ENDER_SLOTS;
	/** Vertical space used by the ender chest panel (vanilla generic_54 top + 3 rows). */
	public static final int TOP_PANEL_HEIGHT = 17 + 3 * 18;
	/**
	 * Vertical space used by the optional 3×3 crafting table panel (same height formula as ender panel).
	 * This matches the region of {@code crafting_table.png} we blit for the panel background.
	 */
	public static final int CRAFTING_TABLE_PANEL_HEIGHT = 17 + 3 * 18;
	/** Number of extra slots added when the crafting table panel is shown (9 grid + 1 result). */
	private static final int CRAFTING_TABLE_TOTAL = 10;

	/**
	 * Constant Y shift used when the crafting table panel is <em>disabled</em>.
	 * When it is enabled, use the instance field {@link #invYShift} instead.
	 */
	public static final int INV_Y_SHIFT = TOP_PANEL_HEIGHT;

	/** True when the 3×3 crafting table panel is included in this menu instance. */
	public final boolean withCraftingTable;
	/**
	 * Total Y shift from the screen top to the start of the player inventory section.
	 * Equals {@link #TOP_PANEL_HEIGHT} + (optionally {@link #CRAFTING_TABLE_PANEL_HEIGHT}).
	 */
	public final int invYShift;

	private static final EquipmentSlot[] ARMOR_GUI_ORDER = new EquipmentSlot[] {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private final CraftingContainer craftSlots;
	private final ResultContainer resultSlots;
	/** Optional 3×3 crafting table container; {@code null} when {@link #withCraftingTable} is false. */
	private final CraftingContainer craftTable;
	/** Result container for the 3×3 panel; {@code null} when {@link #withCraftingTable} is false. */
	private final ResultContainer craftTableResult;
	private final Player owner;
	private final boolean active;
	private final Container enderChest;

	public LuipyInventoryWithEnderMenu(int syncId, Inventory inventory) {
		super(LuipyMenuTypes.INVENTORY_WITH_ENDER, syncId);
		this.withCraftingTable = LuipyUtilsConfigManager.get().showCraftingTableWithInventory;
		this.invYShift = TOP_PANEL_HEIGHT + (withCraftingTable ? CRAFTING_TABLE_PANEL_HEIGHT : 0);

		this.craftSlots = new TransientCraftingContainer(this, 2, 2);
		this.resultSlots = new ResultContainer();
		this.active = !inventory.player.level().isClientSide();
		this.owner = inventory.player;
		this.enderChest = inventory.player.getEnderChestInventory();
		this.enderChest.startOpen(inventory.player);

		int S = invYShift;

		// --- Slots 0–4: result + 2×2 crafting (player inventory style) ---
		addSlot(new ResultSlot(inventory.player, craftSlots, resultSlots, 0, 154, 28 + S));
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 2; col++) {
				addSlot(new Slot(craftSlots, col + row * 2, 98 + col * 18, 18 + row * 18 + S));
			}
		}

		// --- Slots 5–31: ender chest 3×9 ---
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(enderChest, col + row * 9, 8 + col * 18, 18 + row * 18));
			}
		}

		// --- Slots 32–41 (only when withCraftingTable): 3×3 grid + result ---
		if (withCraftingTable) {
			this.craftTable = new TransientCraftingContainer(this, 3, 3);
			this.craftTableResult = new ResultContainer();
			int tableY = TOP_PANEL_HEIGHT; // panel starts immediately below the ender panel
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					addSlot(new Slot(craftTable, col + row * 3, 30 + col * 18, 17 + row * 18 + tableY));
				}
			}
			addSlot(new ResultSlot(inventory.player, craftTable, craftTableResult, 0, 124, 35 + tableY));
		} else {
			this.craftTable = null;
			this.craftTableResult = null;
		}

		// --- Armor, main inventory, hotbar, offhand (all shifted by invYShift) ---
		for (int i = 0; i < 4; i++) {
			final EquipmentSlot equipmentSlot = ARMOR_GUI_ORDER[i];
			addSlot(new Slot(inventory, 39 - i, 8, 8 + i * 18 + S) {
				@Override
				public void setByPlayer(ItemStack stack) {
					ItemStack prev = getItem();
					owner.onEquipItem(equipmentSlot, stack, prev);
					super.setByPlayer(stack);
				}

				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public boolean mayPlace(ItemStack stack) {
					if (stack.isEmpty()) {
						return false;
					}
					return Mob.getEquipmentSlotForItem(stack) == equipmentSlot;
				}

				@Override
				public boolean mayPickup(Player player) {
					ItemStack inSlot = getItem();
					return (inSlot.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(inSlot)) && super.mayPickup(player);
				}
			});
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + (row + 1) * 9, 8 + col * 18, 84 + row * 18 + S));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, 142 + S));
		}
		addSlot(new Slot(inventory, 40, 77, 62 + S) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});
	}

	@Override
	public void slotsChanged(Container container) {
		if (container == craftSlots) {
			CraftingMenu.slotChangedCraftingGrid(this, owner.level(), owner, craftSlots, resultSlots);
		}
		if (withCraftingTable && container == craftTable) {
			CraftingMenu.slotChangedCraftingGrid(this, owner.level(), owner, craftTable, craftTableResult);
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		resultSlots.clearContent();
		if (!player.level().isClientSide()) {
			clearContainer(player, craftSlots);
		}
		if (withCraftingTable && craftTable != null) {
			if (craftTableResult != null) craftTableResult.clearContent();
			if (!player.level().isClientSide()) {
				clearContainer(player, craftTable);
			}
		}
		enderChest.stopOpen(player);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		// O maps menu indices back to vanilla player-slot numbering (0=result, 1-4=2x2, 5-8=armor, 9-35=main, 36-44=hotbar, 45=offhand).
		final int O = ENDER_SLOTS + (withCraftingTable ? CRAFTING_TABLE_TOTAL : 0);
		ItemStack ret = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack moved = slot.getItem();
			ret = moved.copy();
			if (index >= ENDER_SLOT_START && index < ENDER_SLOT_END) {
				// Ender → player inventory
				if (!moveItemStackTo(moved, O + 9, O + 46, true)) {
					return ItemStack.EMPTY;
				}
			} else if (withCraftingTable && index >= ENDER_SLOT_END && index < ENDER_SLOT_END + CRAFTING_TABLE_TOTAL) {
				// Crafting table grid/result → player inventory
				if (!moveItemStackTo(moved, O + 9, O + 45, true)) {
					return ItemStack.EMPTY;
				}
			} else {
				int v = index < CRAFTING_SLOT_COUNT ? index : index - O;
				if (v == 0) {
					// 2×2 result → hotbar+main then drop
					if (!moveItemStackTo(moved, O + 9, O + 45, true)) {
						return ItemStack.EMPTY;
					}
				} else if (v >= 1 && v < 5) {
					// 2×2 crafting grid → player inv
					if (!moveItemStackTo(moved, O + 9, O + 45, false)) {
						return ItemStack.EMPTY;
					}
				} else if (v >= 5 && v < 9) {
					// Armor → player inv
					if (!moveItemStackTo(moved, O + 9, O + 45, false)) {
						return ItemStack.EMPTY;
					}
				} else if (v >= 9 && v < 36) {
					// Main inv → hotbar + offhand (split range to avoid source-slot self-merge).
					if (!moveItemStackTo(moved, O + 36, O + 46, true)) {
						return ItemStack.EMPTY;
					}
				} else if (v >= 36 && v < 45) {
					// Hotbar → main inv
					if (!moveItemStackTo(moved, O + 9, O + 36, true)) {
						return ItemStack.EMPTY;
					}
				} else if (v == 45) {
					// Offhand → main inv + hotbar
					if (!moveItemStackTo(moved, O + 9, O + 45, true)) {
						return ItemStack.EMPTY;
					}
				} else {
					return ItemStack.EMPTY;
				}
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
			if (index == InventoryMenu.RESULT_SLOT) {
				player.drop(moved, false);
			}
		}
		return ret;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != resultSlots
			&& (craftTableResult == null || slot.container != craftTableResult)
			&& super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents finder) {
		craftSlots.fillStackedContents(finder);
	}

	@Override
	public void clearCraftingContent() {
		resultSlots.clearContent();
		craftSlots.clearContent();
	}

	@Override
	public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
		return recipe.matches(craftSlots, owner.level());
	}

	@Override
	public int getResultSlotIndex() {
		return InventoryMenu.RESULT_SLOT;
	}

	@Override
	public int getGridWidth() {
		return craftSlots.getWidth();
	}

	@Override
	public int getGridHeight() {
		return craftSlots.getHeight();
	}

	@Override
	public int getSize() {
		return 5;
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int slotIndex) {
		return slotIndex != getResultSlotIndex();
	}
}
