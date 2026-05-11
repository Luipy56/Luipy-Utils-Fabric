package com.luipy.utilsmod.client.mixin;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
	@SuppressWarnings("unused")
	protected EnchantmentScreenMixin() {
		super(null, null, null);
	}

	/**
	 * Redirects the single-enchantment tooltip call in {@code render} to show ALL enchantments
	 * that will be applied when the player picks that slot. The computation replicates the server's
	 * RNG seeding using the synced {@code enchantmentSeed} data slot, so it works against vanilla
	 * servers without any server-side mod.
	 */
	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;renderComponentTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"
		)
	)
	private void luipy$enhancedEnchantTooltip(GuiGraphics graphics, Font font, List<Component> original, int x, int y) {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.showEnchantmentPreview) {
			graphics.renderComponentTooltip(font, original, x, y);
			return;
		}

		// Determine which of the three enchantment buttons the mouse is over.
		// Matches the vanilla isHovering(60, 14 + 19*l, 108, 17, ...) check from render().
		int hoveredSlot = -1;
		for (int s = 0; s < 3; s++) {
			int relX = x - leftPos - 60;
			int relY = y - topPos - (14 + 19 * s);
			if (relX >= 0 && relX < 108 && relY >= 0 && relY < 17) {
				hoveredSlot = s;
				break;
			}
		}

		if (hoveredSlot < 0) {
			graphics.renderComponentTooltip(font, original, x, y);
			return;
		}

		EnchantmentMenu enchMenu = this.menu;
		int level = enchMenu.costs[hoveredSlot];
		ItemStack item = enchMenu.getSlot(0).getItem();

		List<EnchantmentInstance> allEnchants =
			((EnchantmentMenuInvoker) enchMenu).luipy$callGetEnchantmentList(item, hoveredSlot, level);

		if (allEnchants == null || allEnchants.isEmpty()) {
			graphics.renderComponentTooltip(font, original, x, y);
			return;
		}

		List<Component> enhanced = new ArrayList<>();
		enhanced.add(Component.translatable("luipy-utils-mod.enchantment.preview.header")
			.withStyle(ChatFormatting.GOLD));
		for (EnchantmentInstance ei : allEnchants) {
			enhanced.add(ei.enchantment.getFullname(ei.level).copy().withStyle(ChatFormatting.YELLOW));
		}

		// Keep the lapis/level requirements from the original tooltip (everything after the clue line).
		// The vanilla list is: [0]=enchant clue, [1]=empty, [2]=lapis/level requirement, [3]=level cost.
		if (original.size() > 1) {
			enhanced.add(CommonComponents.EMPTY);
			for (int i = 1; i < original.size(); i++) {
				enhanced.add(original.get(i));
			}
		}

		graphics.renderComponentTooltip(font, enhanced, x, y);
	}
}
