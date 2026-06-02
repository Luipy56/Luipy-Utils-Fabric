package com.luipy.utilsmod.inventory;

import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.inventory.workstation.UnifiedWorkstationLayout;
import com.luipy.utilsmod.inventory.workstation.WorkstationKind;
import com.luipy.utilsmod.inventory.workstation.WorkstationPanelHost;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Luipy unified menu (R): optional workstation panels in a left column; optional ender chest and
 * crafting table above a compact player inventory in the right column.
 *
 * <p>Panel order (top to bottom): left column — anvil, smithing, cartography, grindstone,
 * stonecutter, loom (each omitted when disabled or gate fails); right column — ender, craft, player.
 *
 * <p>Slot indices are assigned at construction: left workstations first, then right-column panels,
 * then player main/hotbar/offhand. See {@link #workstationHost} for workstation slot ranges.
 */
public class LuipyUnifiedMenu extends RecipeBookMenu<CraftingContainer> {
	public static final int TOP_LAYOUT_PADDING = 0;
	public static final int ENDER_SLOT_COUNT = 27;
	public static final int CRAFT_GRID_COUNT = 9;
	public static final int ENDER_PANEL_HEIGHT = 17 + 3 * 18;
	public static final int CRAFTING_PANEL_HEIGHT = 17 + 3 * 18;
	public static final int PLAYER_TEXTURE_SRC_V = 51;
	public static final int PLAYER_PANEL_HEIGHT = 115;
	public static final int PLAYER_OFFHAND_Y = 11;
	public static final int PLAYER_MAIN_Y = 33;
	public static final int PLAYER_HOTBAR_Y = 91;
	public static final int RIGHT_COLUMN_X_OFFSET = UnifiedWorkstationLayout.LEFT_COLUMN_WIDTH;

	public final List<WorkstationKind> enabledWorkstations;
	public static final int MAIN_BLOCK_WIDTH = 176;

	public final int rightColumnX;
	public final int leftColumnHeight;
	public final int mainBlockHeight;
	public final int rightColumnContentTop;
	public final boolean withEnder;
	public final boolean withCrafting;
	public final int playerSectionTop;
	public final int totalContentHeight;

	public final int enderStart;
	public final int enderEnd;
	public final int craftGridStart;
	public final int craftResultIndex;
	public final int mainStart;
	public final int hotbarStart;
	public final int offhandIndex;
	public final int playerEndExclusive;

	public final WorkstationPanelHost workstationHost;

	private final CraftingContainer craftGrid;
	private final ResultContainer craftResult;
	private final Player owner;
	private final Container enderChest;

	public LuipyUnifiedMenu(int syncId, Inventory inventory) {
		super(LuipyMenuTypes.UNIFIED, syncId);
		var cfg = LuipyUtilsConfigManager.get();
		Player player = inventory.player;
		this.enabledWorkstations = UnifiedWorkstationLayout.resolve(cfg, player, player.level());
		this.rightColumnX = enabledWorkstations.isEmpty() ? 0 : RIGHT_COLUMN_X_OFFSET;
		this.withEnder = cfg.showEnderChestWithInventory
			&& com.luipy.utilsmod.ender.EnderGateEvaluation.passesGate(cfg, player, player.level());
		this.withCrafting = cfg.showCraftingTableWithInventory;

		Map<WorkstationKind, Integer> panelTops = UnifiedWorkstationLayout.panelTopOffsets(
			enabledWorkstations, TOP_LAYOUT_PADDING);
		this.leftColumnHeight = UnifiedWorkstationLayout.leftColumnHeight(enabledWorkstations, TOP_LAYOUT_PADDING);

		int rightStackHeight = (withEnder ? ENDER_PANEL_HEIGHT : 0) + (withCrafting ? CRAFTING_PANEL_HEIGHT : 0);
		this.mainBlockHeight = rightStackHeight + PLAYER_PANEL_HEIGHT;
		this.rightColumnContentTop = TOP_LAYOUT_PADDING;
		this.playerSectionTop = rightColumnContentTop + rightStackHeight;
		this.totalContentHeight = Math.max(leftColumnHeight, playerSectionTop + PLAYER_PANEL_HEIGHT);

		this.craftGrid = new TransientCraftingContainer(this, 3, 3);
		this.craftResult = new ResultContainer();
		this.owner = player;
		this.enderChest = player.getEnderChestInventory();
		this.workstationHost = new WorkstationPanelHost(enabledWorkstations);
		this.workstationHost.initDelegates(inventory);

		if (withEnder) {
			enderChest.startOpen(player);
		}

		int idx = 0;
		idx = installWorkstationSlots(idx, panelTops, inventory);

		int es = -1;
		int ee = -1;
		int cgs = -1;
		int cri = -1;

		if (withEnder) {
			es = idx;
			int enderY = rightColumnContentTop;
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 9; col++) {
					addSlot(new Slot(enderChest, col + row * 9, rightColumnX + 8 + col * 18, enderY + 18 + row * 18));
					idx++;
				}
			}
			ee = idx;
		}

		if (withCrafting) {
			int tableY = rightColumnContentTop + (withEnder ? ENDER_PANEL_HEIGHT : 0);
			cgs = idx;
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					addSlot(new Slot(craftGrid, col + row * 3, rightColumnX + 30 + col * 18, 17 + row * 18 + tableY));
					idx++;
				}
			}
			cri = idx;
			addSlot(new ResultSlot(inventory.player, craftGrid, craftResult, 0, rightColumnX + 124, 35 + tableY));
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
				addSlot(new Slot(inventory, col + (row + 1) * 9, rightColumnX + 8 + col * 18, ps + PLAYER_MAIN_Y + row * 18));
				idx++;
			}
		}
		this.hotbarStart = idx;
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, rightColumnX + 8 + col * 18, ps + PLAYER_HOTBAR_Y));
			idx++;
		}
		this.offhandIndex = idx;
		addSlot(new Slot(inventory, 40, rightColumnX + 77, ps + PLAYER_OFFHAND_Y) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});
		idx++;
		this.playerEndExclusive = idx;
	}

	private int installWorkstationSlots(int idx, Map<WorkstationKind, Integer> panelTops, Inventory inventory) {
		Player player = inventory.player;
		for (WorkstationKind kind : enabledWorkstations) {
			int top = panelTops.getOrDefault(kind, TOP_LAYOUT_PADDING);
			int start = idx;
			idx = switch (kind) {
				case ANVIL -> installAnvil(idx, top, player);
				case SMITHING -> installSmithing(idx, top, player);
				case CARTOGRAPHY -> installCartography(idx, top, player);
				case GRINDSTONE -> installGrindstone(idx, top, player);
				case STONECUTTER -> installStonecutter(idx, top, player);
				case LOOM -> installLoom(idx, top, player);
			};
			workstationHost.recordRange(kind, start, idx, top);
		}
		return idx;
	}

	private int installAnvil(int idx, int top, Player player) {
		var delegate = workstationHost.anvil();
		addDataSlot(delegate.costSlot());
		addSlot(new Slot(delegate.input(), 0, 27, 47 + top));
		idx++;
		addSlot(new Slot(delegate.input(), 1, 76, 47 + top));
		idx++;
		addSlot(combinerResultSlot(delegate, 134, 47 + top));
		return idx + 1;
	}

	private int installSmithing(int idx, int top, Player player) {
		var delegate = workstationHost.smithing();
		addSlot(mayPlaceSlot(delegate.input(), 0, 8, 48 + top, delegate::mayPlaceTemplate));
		idx++;
		addSlot(mayPlaceSlot(delegate.input(), 1, 26, 48 + top, delegate::mayPlaceBase));
		idx++;
		addSlot(mayPlaceSlot(delegate.input(), 2, 44, 48 + top, delegate::mayPlaceAddition));
		idx++;
		addSlot(combinerResultSlot(delegate, 98, 48 + top));
		return idx + 1;
	}

	private int installCartography(int idx, int top, Player player) {
		var delegate = workstationHost.cartography();
		addSlot(mayPlaceSlot(delegate.input(), 0, 15, 15 + top, stack -> stack.is(Items.FILLED_MAP)));
		idx++;
		addSlot(mayPlaceSlot(delegate.input(), 1, 15, 52 + top,
			stack -> stack.is(Items.PAPER) || stack.is(Items.MAP) || stack.is(Items.GLASS_PANE)));
		idx++;
		addSlot(customResultSlot(delegate.result(), 145, 39 + top, player, delegate::onTake));
		return idx + 1;
	}

	private int installGrindstone(int idx, int top, Player player) {
		var delegate = workstationHost.grindstone();
		addSlot(mayPlaceSlot(delegate.input(), 0, 49, 19 + top, delegate::mayPlaceInput));
		idx++;
		addSlot(mayPlaceSlot(delegate.input(), 1, 49, 40 + top, delegate::mayPlaceInput));
		idx++;
		addSlot(customResultSlot(delegate.result(), 129, 34 + top, player, delegate::onTake));
		return idx + 1;
	}

	private int installStonecutter(int idx, int top, Player player) {
		var delegate = workstationHost.stonecutter();
		addDataSlot(delegate.recipeIndexSlot());
		addSlot(new Slot(delegate.input(), 0, 20, 33 + top));
		idx++;
		addSlot(customResultSlot(delegate.result(), 143, 33 + top, player, delegate::onTake));
		return idx + 1;
	}

	private int installLoom(int idx, int top, Player player) {
		var delegate = workstationHost.loom();
		addDataSlot(delegate.patternIndexSlot());
		addSlot(mayPlaceSlot(delegate.input(), 0, 13, 26 + top, WorkstationPanelHost::mayPlaceBanner));
		idx++;
		addSlot(mayPlaceSlot(delegate.input(), 1, 33, 26 + top, WorkstationPanelHost::mayPlaceDye));
		idx++;
		addSlot(mayPlaceSlot(delegate.input(), 2, 23, 45 + top, WorkstationPanelHost::mayPlaceBannerPattern));
		idx++;
		addSlot(customResultSlot(delegate.output(), 143, 58 + top, player, delegate::onTake));
		return idx + 1;
	}

	private Slot mayPlaceSlot(Container container, int index, int x, int y, java.util.function.Predicate<ItemStack> mayPlace) {
		return new Slot(container, index, x, y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return mayPlace.test(stack);
			}
		};
	}

	private Slot combinerResultSlot(WorkstationPanelHost.CombinerDelegate delegate, int x, int y) {
		return new Slot(delegate.result(), 0, x, y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player p) {
				return delegate.mayPickup(p, hasItem());
			}

			@Override
			public void onTake(Player p, ItemStack stack) {
				delegate.onTake(p, stack);
			}
		};
	}

	private interface ResultTake {
		void onTake(Player player, ItemStack stack);
	}

	private Slot customResultSlot(Container result, int x, int y, Player player, ResultTake take) {
		return new Slot(result, 0, x, y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(Player p, ItemStack stack) {
				take.onTake(p, stack);
			}
		};
	}

	@Override
	public void slotsChanged(Container container) {
		workstationHost.onSlotsChanged(container);
		if (withCrafting && container == craftGrid) {
			CraftingMenu.slotChangedCraftingGrid(this, owner.level(), owner, craftGrid, craftResult);
		}
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonId) {
		if (workstationHost.stonecutter() != null
			&& workstationHost.clickMenuButton(player, WorkstationKind.STONECUTTER, buttonId)) {
			return true;
		}
		if (workstationHost.loom() != null
			&& workstationHost.clickMenuButton(player, WorkstationKind.LOOM, buttonId)) {
			return true;
		}
		return super.clickMenuButton(player, buttonId);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		workstationHost.removed(player);
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

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack ret = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack moved = slot.getItem();
		ret = moved.copy();

		if (isWorkstationIndex(index)) {
			if (!moveToPlayerMainAndHotbar(moved, false)) {
				return ItemStack.EMPTY;
			}
		} else if (withEnder && index >= enderStart && index < enderEnd) {
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
			if (!moveItemStackTo(moved, hotbarStart, offhandIndex, false)) {
				if (!moveToOptionalPanels(moved)) {
					return ItemStack.EMPTY;
				}
			}
		} else if (index >= hotbarStart && index < offhandIndex) {
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

	public boolean isWorkstationSlot(Slot slot) {
		int index = slots.indexOf(slot);
		return index >= 0 && isWorkstationIndex(index);
	}

	private boolean isWorkstationIndex(int index) {
		for (WorkstationPanelHost.SlotRange range : workstationHost.slotRanges().values()) {
			if (index >= range.startInclusive() && index < range.endExclusive()) {
				return true;
			}
		}
		return false;
	}

	private boolean moveToPlayerMainAndHotbar(ItemStack moved, boolean reverse) {
		if (moveItemStackTo(moved, mainStart, hotbarStart, reverse)) {
			return true;
		}
		return moveItemStackTo(moved, hotbarStart, offhandIndex, reverse);
	}

	private boolean moveToOptionalPanels(ItemStack moved) {
		for (WorkstationPanelHost.SlotRange range : workstationHost.slotRanges().values()) {
			if (moveItemStackTo(moved, range.startInclusive(), range.endExclusive(), false)) {
				return true;
			}
		}
		if (withEnder && moveItemStackTo(moved, enderStart, enderEnd, false)) {
			return true;
		}
		return withCrafting && moveItemStackTo(moved, craftGridStart, craftResultIndex, false);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		if (withCrafting && slot.container == craftResult) {
			return false;
		}
		return !isWorkstationResultSlot(slot);
	}

	private boolean isWorkstationResultSlot(Slot slot) {
		int menuIndex = slots.indexOf(slot);
		if (menuIndex < 0) {
			return false;
		}
		for (WorkstationKind kind : enabledWorkstations) {
			WorkstationPanelHost.SlotRange range = workstationHost.slotRanges().get(kind);
			if (range != null && menuIndex == range.endExclusive() - 1) {
				return true;
			}
		}
		return false;
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
