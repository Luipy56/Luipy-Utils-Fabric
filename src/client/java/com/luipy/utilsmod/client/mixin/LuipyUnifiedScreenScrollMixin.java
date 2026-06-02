package com.luipy.utilsmod.client.mixin;

import com.luipy.utilsmod.client.inventory.LuipyUnifiedScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
abstract class LuipyUnifiedScreenScrollMixin {
	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"
		)
	)
	private void luipy$renderSlotWithScroll(AbstractContainerScreen screen, GuiGraphics graphics, Slot slot) {
		AbstractContainerScreenInvoker invoker = (AbstractContainerScreenInvoker) screen;
		if (screen instanceof LuipyUnifiedScreen unified) {
			graphics.pose().pushPose();
			graphics.pose().translate(0.0f, (float) -unified.luipyScrollOffset(), 0.0f);
			invoker.luipy$renderSlot(graphics, slot);
			graphics.pose().popPose();
			return;
		}
		invoker.luipy$renderSlot(graphics, slot);
	}
}
