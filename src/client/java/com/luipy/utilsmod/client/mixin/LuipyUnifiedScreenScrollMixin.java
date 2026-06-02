package com.luipy.utilsmod.client.mixin;

import com.luipy.utilsmod.client.inventory.LuipyUnifiedScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
abstract class LuipyUnifiedScreenScrollMixin {
	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

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
	private void luipy$renderSlotHighlightWithScroll(
		AbstractContainerScreen<?> screen,
		GuiGraphics graphics,
		int x,
		int y,
		int blitOffset
	) {
		if (screen instanceof LuipyUnifiedScreen unified) {
			unified.luipyRenderSlotHighlight(graphics, x, y, blitOffset);
			return;
		}
		AbstractContainerScreen.renderSlotHighlight(graphics, x, y, blitOffset);
	}

	@Redirect(
		method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;isHovering(IIIIDD)Z"
		)
	)
	private boolean luipy$workstationHover(
		AbstractContainerScreen<?> screen,
		Slot slot,
		int slotX,
		int slotY,
		int width,
		int height,
		double mouseX,
		double mouseY
	) {
		if (screen instanceof LuipyUnifiedScreen unified) {
			return unified.luipyIsHoveringWorkstationAdjusted(slot, mouseX, mouseY);
		}
		return luipy$rectHover(slotX, slotY, width, height, mouseX, mouseY);
	}

	private boolean luipy$rectHover(int slotX, int slotY, int width, int height, double mouseX, double mouseY) {
		double d = mouseX - (double) this.leftPos;
		double e = mouseY - (double) this.topPos;
		return d >= (double) (slotX - 1)
			&& d < (double) (slotX + width + 1)
			&& e >= (double) (slotY - 1)
			&& e < (double) (slotY + height + 1);
	}
}
