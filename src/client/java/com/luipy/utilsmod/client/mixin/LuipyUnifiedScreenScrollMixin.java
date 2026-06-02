package com.luipy.utilsmod.client.mixin;

import com.luipy.utilsmod.client.inventory.LuipyUnifiedScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
abstract class LuipyUnifiedScreenScrollMixin {
	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"
		)
	)
	private void luipy$renderSlotWithScroll(AbstractContainerScreen<?> screen, GuiGraphics graphics, Slot slot) {
		AbstractContainerScreenInvoker invoker = (AbstractContainerScreenInvoker) screen;
		if (screen instanceof LuipyUnifiedScreen unified && unified.luipyIsWorkstationSlot(slot)) {
			graphics.pose().pushPose();
			graphics.pose().translate(0.0f, (float) -unified.luipyWorkstationScrollOffset(), 0.0f);
			invoker.luipy$renderSlot(graphics, slot);
			graphics.pose().popPose();
			return;
		}
		invoker.luipy$renderSlot(graphics, slot);
	}

	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;III)V"
		)
	)
	private void luipy$renderSlotHighlightWithScroll(GuiGraphics graphics, int x, int y, int blitOffset) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)(Object) this;
		if (screen instanceof LuipyUnifiedScreen unified) {
			unified.luipyRenderSlotHighlight(graphics, x, y, blitOffset);
			return;
		}
		AbstractContainerScreen.renderSlotHighlight(graphics, x, y, blitOffset);
	}

	@Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
	private void luipy$workstationHover(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof LuipyUnifiedScreen unified) {
			cir.setReturnValue(unified.luipyIsHoveringWorkstationAdjusted(slot, mouseX, mouseY));
		}
	}
}
