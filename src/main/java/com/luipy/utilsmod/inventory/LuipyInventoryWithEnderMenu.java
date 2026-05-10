package com.luipy.utilsmod.inventory;

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
 * Survival inventory (crafting, armor, inv, hotbar, offhand) plus 3×9 ender chest rows on top.
 * Slot layout matches {@link InventoryMenu} with all player-facing Y coordinates shifted by {@link #INV_Y_SHIFT}.
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
	public static final int INV_Y_SHIFT = TOP_PANEL_HEIGHT;

	private static final EquipmentSlot[] ARMOR_GUI_ORDER = new EquipmentSlot[] {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private final CraftingContainer craftSlots;
	private final ResultContainer resultSlots;
	private final Player owner;
	private final boolean active;
	private final Container enderChest;

	public LuipyInventoryWithEnderMenu(int syncId, Inventory inventory) {
		super(LuipyMenuTypes.INVENTORY_WITH_ENDER, syncId);
		this.craftSlots = new TransientCraftingContainer(this, 2, 2);
		this.resultSlots = new ResultContainer();
		this.active = !inventory.player.level().isClientSide();
		this.owner = inventory.player;
		this.enderChest = inventory.player.getEnderChestInventory();
		this.enderChest.startOpen(inventory.player);

		int S = INV_Y_SHIFT;
		addSlot(new ResultSlot(inventory.player, craftSlots, resultSlots, 0, 154, 28 + S));
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 2; col++) {
				addSlot(new Slot(craftSlots, col + row * 2, 98 + col * 18, 18 + row * 18 + S));
			}
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(enderChest, col + row * 9, 8 + col * 18, 18 + row * 18));
			}
		}
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
		CraftingMenu.slotChangedCraftingGrid(this, owner.level(), owner, craftSlots, resultSlots);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		resultSlots.clearContent();
		if (!player.level().isClientSide()) {
			clearContainer(player, craftSlots);
		}
		enderChest.stopOpen(player);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		final int O = ENDER_SLOTS;
		ItemStack ret = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack moved = slot.getItem();
			ret = moved.copy();
			if (index >= ENDER_SLOT_START && index < ENDER_SLOT_END) {
				if (!moveItemStackTo(moved, O + 9, O + 46, true)) {
					return ItemStack.EMPTY;
				}
			} else {
				int v = index < CRAFTING_SLOT_COUNT ? index : index - O;
				if (v == 0) {
					if (!moveItemStackTo(moved, O + 9, O + 45, true)) {
						return ItemStack.EMPTY;
					}
				} else if (v >= 1 && v < 5) {
					if (!moveItemStackTo(moved, O + 9, O + 45, false)) {
						return ItemStack.EMPTY;
					}
				} else if (v >= 5 && v < 9) {
					if (!moveItemStackTo(moved, O + 9, O + 45, false)) {
						return ItemStack.EMPTY;
					}
				} else if (v >= 9) {
					// Main/hotbar/offhand: vanilla-style shift within player slots only (no quick-move into ender).
					if (!moveItemStackTo(moved, O + 9, O + 46, true)) {
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
		return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
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
