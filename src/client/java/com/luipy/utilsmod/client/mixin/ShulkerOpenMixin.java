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

		Slot slot = ((AbstractContainerScreenInvoker) this).luipy$findSlot(mouseX, mouseY);
		if (slot == null || !slot.hasItem()) return;
		if (!(slot.container instanceof Inventory)) return;

		ItemStack stack = slot.getItem();
		if (!(Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock)) return;

		Minecraft mc = Minecraft.getInstance();

		if (!menu.getCarried().isEmpty()) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_carrying_item");
			return;
		}

		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled) {
			return;
		}
		if (!cfg.allowOpenShulkerFromInventory) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_feature_disabled");
			return;
		}

		if (!LuipyClientState.serverHasLuipyMod() && mc.getSingleplayerServer() == null) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_requires_mod_on_server");
			return;
		}

		if (mc.player != null && mc.player.isSpectator()) {
			LuipyClientMessages.featureFailure(mc, "luipy-utils-mod.message.shulker_spectator");
			return;
		}

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(slot.index);
		ClientPlayNetworking.send(LuipyNetworking.C2S_OPEN_SHULKER, buf);
		cir.setReturnValue(true);
	}
}
