package com.luipy.utilsmod.client.ender;

import com.luipy.utilsmod.client.LuipyClientState;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.ender.EnderGateAccess;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Shows an ender chest icon left of the hotbar when ender access in the unified menu is available.
 */
public final class EnderGateHudIndicator {
	private static final int HOTBAR_LEFT_OFFSET = 91;
	private static final int ICON_GAP = 4;
	private static final int HOTBAR_Y_OFFSET = 22;

	private EnderGateHudIndicator() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register(EnderGateHudIndicator::render);
	}

	static boolean shouldShow(Minecraft client) {
		if (client.player == null || client.level == null || client.options.hideGui) {
			return false;
		}
		if (client.player.isCreative()) {
			return false;
		}
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!EnderGateAccess.enderHudGatePasses(cfg, client.player, client.level)) {
			return false;
		}
		return LuipyClientState.serverHasLuipyMod() || client.getSingleplayerServer() != null;
	}

	private static void render(GuiGraphics guiGraphics, float tickDelta) {
		Minecraft client = Minecraft.getInstance();
		if (!shouldShow(client)) {
			return;
		}

		int centerX = client.getWindow().getGuiScaledWidth() / 2;
		int y = client.getWindow().getGuiScaledHeight() - HOTBAR_Y_OFFSET;
		int x = centerX - HOTBAR_LEFT_OFFSET - 16 - ICON_GAP;

		ItemStack icon = Items.ENDER_CHEST.getDefaultInstance();
		guiGraphics.renderItem(icon, x, y);
		guiGraphics.renderItemDecorations(client.font, icon, x, y);
	}
}
