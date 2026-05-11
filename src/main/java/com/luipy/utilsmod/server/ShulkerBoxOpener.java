package com.luipy.utilsmod.server;

import com.luipy.utilsmod.inventory.LuipyShulkerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class ShulkerBoxOpener {
	private ShulkerBoxOpener() {
	}

	public static void tryOpenFor(ServerPlayer player, int inventorySlotIndex) {
		if (player.isSpectator()) return;

		ItemStack shulkerItem = player.getInventory().getItem(inventorySlotIndex);
		if (shulkerItem.isEmpty() || !(Block.byItem(shulkerItem.getItem()) instanceof ShulkerBoxBlock)) {
			return;
		}

		SimpleContainer container = new SimpleContainer(27);
		CompoundTag beTag = BlockItem.getBlockEntityData(shulkerItem);
		if (beTag != null && beTag.contains("Items", 9)) {
			NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
			ContainerHelper.loadAllItems(beTag, items);
			for (int i = 0; i < 27; i++) container.setItem(i, items.get(i));
		}

		player.openMenu(new SimpleMenuProvider(
			(syncId, inv, p) -> new LuipyShulkerMenu(syncId, inv, container, inventorySlotIndex),
			Component.translatable("luipy-utils-mod.screen.shulker_virtual")
		));
	}
}
