package com.luipy.utilsmod.client.mixin;

import com.luipy.utilsmod.client.ender.LuipyInventoryKeyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Redirect(
		method = "handleKeybinds",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onOpenInventory()V"),
			to = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;<init>(Lnet/minecraft/client/multiplayer/ClientAdvancements;)V"
			)
		)
	)
	private void luipy$interceptInventoryScreen(Minecraft instance, Screen screen) {
		LuipyInventoryKeyHandler.handleInventorySetScreen(instance, screen);
	}
}
