package com.luipy.utilsmod.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * Menu for a shulker box item opened from a player inventory slot.
 * On the server it holds a reference to the player slot index and serializes contents back
 * when the menu is closed. On the client the two-arg constructor is used by the MenuType factory;
 * slot contents arrive via normal menu sync.
 */
public class LuipyShulkerMenu extends AbstractContainerMenu {
	/** Index within the player {@link Inventory} of the shulker item, or {@code -1} on the client. */
	private final int playerSlotIndex;
	private final Inventory playerInventory;
	private final SimpleContainer container;

	/** Client-side constructor (called by the {@link net.minecraft.world.inventory.MenuType} factory). */
	public LuipyShulkerMenu(int syncId, Inventory inventory) {
		this(syncId, inventory, new SimpleContainer(27), -1);
	}

	/** Server-side constructor with the resolved container and originating slot. */
	public LuipyShulkerMenu(int syncId, Inventory inventory, SimpleContainer container, int playerSlotIndex) {
		super(LuipyMenuTypes.SHULKER_VIRTUAL, syncId);
		this.playerSlotIndex = playerSlotIndex;
		this.playerInventory = inventory;
		this.container = container;
		container.startOpen(inventory.player);

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new ShulkerBoxSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
			}
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, 142));
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
		if (slot != null && slot.hasItem()) {
			ItemStack moved = slot.getItem();
			ret = moved.copy();
			if (index < 27) {
				if (!moveItemStackTo(moved, 27, slots.size(), true)) return ItemStack.EMPTY;
			} else {
				if (!moveItemStackTo(moved, 0, 27, false)) return ItemStack.EMPTY;
			}
			if (moved.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
			else slot.setChanged();
		}
		return ret;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		container.stopOpen(player);
		if (playerSlotIndex >= 0 && !player.level().isClientSide()) {
			serializeBackToItem(player);
		}
	}

	private void serializeBackToItem(Player player) {
		ItemStack shulkerItem = playerInventory.getItem(playerSlotIndex);
		if (shulkerItem.isEmpty() || !(Block.byItem(shulkerItem.getItem()) instanceof ShulkerBoxBlock)) {
			// Shulker was moved or replaced: return its contents to the player, drop overflow.
			for (int i = 0; i < 27; i++) {
				ItemStack s = container.getItem(i);
				if (!s.isEmpty() && !player.getInventory().add(s)) {
					player.drop(s, false);
				}
			}
			return;
		}
		NonNullList<ItemStack> nnList = NonNullList.withSize(27, ItemStack.EMPTY);
		for (int i = 0; i < 27; i++) nnList.set(i, container.getItem(i).copy());
		CompoundTag beTag = shulkerItem.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG);
		ContainerHelper.saveAllItems(beTag, nnList, false);
	}

	/** Returns the player-inventory slot index of the shulker item, or {@code -1} on the client. */
	public int getPlayerSlotIndex() {
		return playerSlotIndex;
	}
}
