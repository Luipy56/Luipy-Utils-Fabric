package com.luipy.utilsmod.client.mixin;

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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ShulkerOpenMixin extends Screen {
	@Shadow protected Slot hoveredSlot;
	@Shadow public AbstractContainerMenu menu;

	@SuppressWarnings("unused")
	protected ShulkerOpenMixin() {
		super(null);
	}

	/**
	 * Intercepts shift+right-click on a shulker box item in a player inventory slot and opens it
	 * as a full interactive container instead of performing vanilla quick-move.
	 */
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void luipy$interceptShulkerOpen(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (button != 1 || !hasShiftDown()) return;

		Slot slot = hoveredSlot;
		if (slot == null || !slot.hasItem()) return;
		if (!(slot.container instanceof Inventory)) return;

		if (!menu.getCarried().isEmpty()) return;

		ItemStack stack = slot.getItem();
		if (!(Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock)) return;

		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled || !cfg.allowOpenShulkerFromInventory) return;

		Minecraft mc = Minecraft.getInstance();
		if (!LuipyClientState.serverHasLuipyMod() && mc.getSingleplayerServer() == null) return;

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(slot.index);
		ClientPlayNetworking.send(LuipyNetworking.C2S_OPEN_SHULKER, buf);
		cir.setReturnValue(true);
	}
}
