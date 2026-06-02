package com.luipy.utilsmod.client.mixin;

import com.luipy.utilsmod.client.LuipyClientMessages;
import com.luipy.utilsmod.client.LuipyClientState;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.network.LuipyNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
public abstract class ShulkerOpenMixin extends Screen {
	@Shadow public AbstractContainerMenu menu;

	@Shadow
	protected abstract void slotClicked(Slot slot, int slotId, int button, ClickType clickType);

	@SuppressWarnings("unused")
	protected ShulkerOpenMixin() {
		super(null);
	}

	/**
	 * Intercepts shift+right-click quick-move on a shulker in player inventory and opens the virtual
	 * container instead. Redirect runs after vanilla shift detection ({@code ClickType.QUICK_MOVE}),
	 * so left and right shift behave the same as vanilla quick-move.
	 */
	@Redirect(
		method = "mouseClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V"
		)
	)
	private void luipy$redirectShulkerOpen(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ClickType clickType) {
		if (clickType == ClickType.QUICK_MOVE && button == 1 && luipy$tryOpenShulkerFromSlot(slot)) {
			return;
		}
		this.slotClicked(slot, slotId, button, clickType);
	}

	private boolean luipy$tryOpenShulkerFromSlot(Slot slot) {
		if (slot == null || !slot.hasItem() || !isPlayerInventorySlot(slot)) {
			return false;
		}

		ItemStack stack = slot.getItem();
		if (!(Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock)) {
			return false;
		}

		Minecraft mc = Minecraft.getInstance();

		if (!menu.getCarried().isEmpty()) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_carrying_item");
			return false;
		}

		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled) {
			return false;
		}
		if (!cfg.allowOpenShulkerFromInventory) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_feature_disabled");
			return false;
		}

		if (!LuipyClientState.serverHasLuipyMod() && mc.getSingleplayerServer() == null) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_requires_mod_on_server");
			return false;
		}

		if (mc.player != null && mc.player.isSpectator()) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_spectator");
			return false;
		}

		int inventorySlotIndex = slot.getContainerSlot();
		if (inventorySlotIndex < 0 || inventorySlotIndex > 35) {
			return false;
		}

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(inventorySlotIndex);
		ClientPlayNetworking.send(LuipyNetworking.C2S_OPEN_SHULKER, buf);
		return true;
	}

	private static boolean isPlayerInventorySlot(Slot slot) {
		Container container = slot.container;
		if (container instanceof Inventory) {
			return true;
		}
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null && container == mc.player.getInventory();
	}
}
