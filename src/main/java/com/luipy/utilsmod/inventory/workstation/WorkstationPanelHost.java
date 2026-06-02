package com.luipy.utilsmod.inventory.workstation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;

/**
 * Headless vanilla workstation delegates for {@link com.luipy.utilsmod.inventory.LuipyUnifiedMenu}.
 */
public final class WorkstationPanelHost {
	public record SlotRange(int startInclusive, int endExclusive, int panelTop) {
	}

	public interface CombinerDelegate {
		ResultContainer result();

		boolean mayPickup(Player player, boolean hasItem);

		void onTake(Player player, ItemStack stack);
	}

	private final Map<WorkstationKind, SlotRange> slotRanges = new EnumMap<>(WorkstationKind.class);
	private final List<WorkstationKind> enabledKinds;
	private AnvilDelegate anvil;
	private SmithingDelegate smithing;
	private CartographyDelegate cartography;
	private GrindstoneDelegate grindstone;
	private StonecutterDelegate stonecutter;
	private LoomDelegate loom;

	public WorkstationPanelHost(List<WorkstationKind> enabledKinds) {
		this.enabledKinds = List.copyOf(enabledKinds);
	}

	public List<WorkstationKind> enabledKinds() {
		return enabledKinds;
	}

	public Map<WorkstationKind, SlotRange> slotRanges() {
		return slotRanges;
	}

	public void initDelegates(Inventory inventory) {
		for (WorkstationKind kind : enabledKinds) {
			switch (kind) {
				case ANVIL -> anvil = new AnvilDelegate(inventory);
				case SMITHING -> smithing = new SmithingDelegate(inventory);
				case CARTOGRAPHY -> cartography = new CartographyDelegate(inventory);
				case GRINDSTONE -> grindstone = new GrindstoneDelegate(inventory);
				case STONECUTTER -> stonecutter = new StonecutterDelegate(inventory);
				case LOOM -> loom = new LoomDelegate(inventory);
			}
		}
	}

	public void recordRange(WorkstationKind kind, int start, int end, int panelTop) {
		slotRanges.put(kind, new SlotRange(start, end, panelTop));
	}

	public void onSlotsChanged(Container container) {
		if (anvil != null && container == anvil.input()) {
			anvil.updateResult();
		}
		if (smithing != null && container == smithing.input()) {
			smithing.updateResult();
		}
		if (cartography != null && container == cartography.input()) {
			cartography.onInputChanged();
		}
		if (grindstone != null && container == grindstone.input()) {
			grindstone.updateResult();
		}
		if (stonecutter != null && container == stonecutter.input()) {
			stonecutter.onInputChanged();
		}
		if (loom != null && container == loom.input()) {
			loom.onInputChanged();
		}
	}

	public boolean clickMenuButton(Player player, WorkstationKind kind, int buttonId) {
		return switch (kind) {
			case STONECUTTER -> stonecutter != null && stonecutter.clickMenuButton(player, buttonId);
			case LOOM -> loom != null && loom.clickMenuButton(player, buttonId);
			default -> false;
		};
	}

	public void removed(Player player) {
		if (anvil != null) {
			clearContainer(player, anvil.input());
		}
		if (smithing != null) {
			clearContainer(player, smithing.input());
		}
		if (cartography != null) {
			cartography.clear(player);
		}
		if (grindstone != null) {
			clearContainer(player, grindstone.input());
		}
		if (stonecutter != null) {
			stonecutter.clear(player);
		}
		if (loom != null) {
			clearContainer(player, loom.input());
		}
	}

	public AnvilDelegate anvil() {
		return anvil;
	}

	public SmithingDelegate smithing() {
		return smithing;
	}

	public CartographyDelegate cartography() {
		return cartography;
	}

	public GrindstoneDelegate grindstone() {
		return grindstone;
	}

	public StonecutterDelegate stonecutter() {
		return stonecutter;
	}

	public LoomDelegate loom() {
		return loom;
	}

	static void clearContainer(Player player, Container container) {
		if (!player.level().isClientSide()) {
			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.removeItemNoUpdate(i);
				if (!stack.isEmpty()) {
					player.drop(stack, false);
				}
			}
		}
	}

	public static final class AnvilDelegate extends AnvilMenu implements CombinerDelegate {
		AnvilDelegate(Inventory inventory) {
			super(-999, inventory, ContainerLevelAccess.NULL);
			this.slots.clear();
		}

		public Container input() {
			return inputSlots;
		}

		@Override
		public ResultContainer result() {
			return resultSlots;
		}

		public DataSlot costSlot() {
			return this.cost;
		}

		public void updateResult() {
			createResult();
		}

		@Override
		public boolean mayPickup(Player player, boolean hasItem) {
			return super.mayPickup(player, hasItem);
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			super.onTake(player, stack);
		}
	}

	public static final class SmithingDelegate extends SmithingMenu implements CombinerDelegate {
		private final List<SmithingRecipe> smithingRecipes;

		SmithingDelegate(Inventory inventory) {
			super(-999, inventory, ContainerLevelAccess.NULL);
			this.slots.clear();
			this.smithingRecipes = inventory.player.level().getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
		}

		public Container input() {
			return inputSlots;
		}

		@Override
		public ResultContainer result() {
			return resultSlots;
		}

		public void updateResult() {
			createResult();
		}

		public boolean mayPlaceTemplate(ItemStack stack) {
			return smithingRecipes.stream().anyMatch(r -> r.isTemplateIngredient(stack));
		}

		public boolean mayPlaceBase(ItemStack stack) {
			return smithingRecipes.stream().anyMatch(r -> r.isBaseIngredient(stack));
		}

		public boolean mayPlaceAddition(ItemStack stack) {
			return smithingRecipes.stream().anyMatch(r -> r.isAdditionIngredient(stack));
		}

		@Override
		public boolean mayPickup(Player player, boolean hasItem) {
			return super.mayPickup(player, hasItem);
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			super.onTake(player, stack);
		}
	}

	public static final class CartographyDelegate extends CartographyTableMenu {
		CartographyDelegate(Inventory inventory) {
			super(-999, inventory, ContainerLevelAccess.NULL);
			this.slots.clear();
		}

		public Container input() {
			return container;
		}

		public ResultContainer result() {
			return this.resultContainer;
		}

		public void onInputChanged() {
			slotsChanged(container);
		}

		public void onTake(Player player, ItemStack stack) {
			Container inv = input();
			if (!inv.getItem(0).isEmpty()) {
				inv.getItem(0).shrink(1);
			}
			if (!inv.getItem(1).isEmpty()) {
				inv.getItem(1).shrink(1);
			}
			stack.getItem().onCraftedBy(stack, player.level(), player);
		}

		public void clear(Player player) {
			result().removeItemNoUpdate(0);
			clearContainer(player, input());
		}
	}

	public static final class GrindstoneDelegate extends GrindstoneMenu {
		GrindstoneDelegate(Inventory inventory) {
			super(-999, inventory, ContainerLevelAccess.NULL);
			this.slots.clear();
		}

		public Container input() {
			return this.repairSlots;
		}

		public ResultContainer result() {
			return (ResultContainer) this.resultSlots;
		}

		public void updateResult() {
			slotsChanged(this.repairSlots);
		}

		public boolean mayPlaceInput(ItemStack stack) {
			return stack.isDamageableItem() || stack.is(Items.ENCHANTED_BOOK) || stack.isEnchanted();
		}

		public void onTake(Player player, ItemStack stack) {
			this.repairSlots.setItem(0, ItemStack.EMPTY);
			this.repairSlots.setItem(1, ItemStack.EMPTY);
		}
	}

	public static final class StonecutterDelegate extends StonecutterMenu {
		StonecutterDelegate(Inventory inventory) {
			super(-999, inventory, ContainerLevelAccess.NULL);
			this.slots.clear();
		}

		public Container input() {
			return container;
		}

		public ResultContainer result() {
			return this.resultContainer;
		}

		public DataSlot recipeIndexSlot() {
			return this.selectedRecipeIndex;
		}

		public void onInputChanged() {
			slotsChanged(container);
		}

		public void onTake(Player player, ItemStack stack) {
			stack.onCraftedBy(player.level(), player, stack.getCount());
			result().awardUsedRecipes(player, List.of(this.inputSlot.getItem()));
			ItemStack remainder = this.inputSlot.remove(1);
			if (!remainder.isEmpty()) {
				setupResultSlot();
			}
		}

		public void clear(Player player) {
			result().removeItemNoUpdate(0);
			clearContainer(player, input());
		}
	}

	public static final class LoomDelegate extends LoomMenu {
		LoomDelegate(Inventory inventory) {
			super(-999, inventory, ContainerLevelAccess.NULL);
			this.slots.clear();
		}

		public Container input() {
			return this.inputContainer;
		}

		public Container output() {
			return this.outputContainer;
		}

		public DataSlot patternIndexSlot() {
			return this.selectedBannerPatternIndex;
		}

		public void onInputChanged() {
			slotsChanged(this.inputContainer);
		}

		public void onTake(Player player, ItemStack stack) {
			this.bannerSlot.remove(1);
			this.dyeSlot.remove(1);
			if (!this.bannerSlot.hasItem() || !this.dyeSlot.hasItem()) {
				this.selectedBannerPatternIndex.set(-1);
			}
		}
	}

	public static boolean mayPlaceBanner(ItemStack stack) {
		return stack.getItem() instanceof BannerItem;
	}

	public static boolean mayPlaceDye(ItemStack stack) {
		return stack.getItem() instanceof DyeItem;
	}

	public static boolean mayPlaceBannerPattern(ItemStack stack) {
		return stack.getItem() instanceof BannerPatternItem;
	}
}
