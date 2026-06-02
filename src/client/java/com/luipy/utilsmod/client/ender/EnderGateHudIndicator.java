package com.luipy.utilsmod.client.ender;

import com.luipy.utilsmod.client.LuipyClientState;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.ender.EnderGateAccess;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Shows an ender chest icon left of the hotbar when ender access in the unified menu is available.
 */
public final class EnderGateHudIndicator {
	private static final int HOTBAR_LEFT_OFFSET = 91;
	/** Vanilla {@code Gui.renderHotbar}: offhand item X is {@code center - 91 - 26} when offhand is on the left. */
	private static final int OFFHAND_LEFT_OFFSET = 26;
	private static final int ICON_GAP = 8;
	/** Matches {@code Gui.renderHotbar}: background top is 22px above the screen bottom. */
	private static final int HOTBAR_BOTTOM_OFFSET = 22;
	private static final int HOTBAR_BACKGROUND_HEIGHT = 22;
	private static final int HOTBAR_ITEM_SIZE = 16;

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
		int screenHeight = client.getWindow().getGuiScaledHeight();
		int hotbarTop = screenHeight - HOTBAR_BOTTOM_OFFSET;
		int y = hotbarTop + (HOTBAR_BACKGROUND_HEIGHT - HOTBAR_ITEM_SIZE) / 2;
		int x = iconX(client, centerX);

		ItemStack icon = Items.ENDER_CHEST.getDefaultInstance();
		guiGraphics.renderItem(icon, x, y);
		guiGraphics.renderItemDecorations(client.font, icon, x, y);
	}

	private static int iconX(Minecraft client, int centerX) {
		HumanoidArm offhandArm = client.player.getMainArm().getOpposite();
		if (offhandArm == HumanoidArm.LEFT) {
			int offhandLeft = centerX - HOTBAR_LEFT_OFFSET - OFFHAND_LEFT_OFFSET;
			return offhandLeft - ICON_GAP - HOTBAR_ITEM_SIZE;
		}
		return centerX - HOTBAR_LEFT_OFFSET - HOTBAR_ITEM_SIZE - ICON_GAP;
	}
}
