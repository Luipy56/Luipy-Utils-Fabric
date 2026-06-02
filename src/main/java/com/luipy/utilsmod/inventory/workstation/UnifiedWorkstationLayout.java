package com.luipy.utilsmod.inventory.workstation;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Resolves which workstation panels appear in the unified menu for a player.
 */
public final class UnifiedWorkstationLayout {
	public static final int LEFT_COLUMN_WIDTH = 176;

	private UnifiedWorkstationLayout() {
	}

	public static List<WorkstationKind> resolve(LuipyUtilsConfig cfg, Player player, Level level) {
		List<WorkstationKind> enabled = new ArrayList<>();
		for (WorkstationKind kind : WorkstationKind.values()) {
			if (WorkstationGateEvaluation.passesGate(cfg, player, level, kind)) {
				enabled.add(kind);
			}
		}
		return enabled;
	}

	public static int leftColumnHeight(List<WorkstationKind> workstations, int topPadding) {
		int height = topPadding;
		for (WorkstationKind kind : workstations) {
			height += kind.panelHeight;
		}
		return height;
	}

	public static Map<WorkstationKind, Integer> panelTopOffsets(List<WorkstationKind> workstations, int topPadding) {
		Map<WorkstationKind, Integer> tops = new EnumMap<>(WorkstationKind.class);
		int y = topPadding;
		for (WorkstationKind kind : workstations) {
			tops.put(kind, y);
			y += kind.panelHeight;
		}
		return tops;
	}
}
