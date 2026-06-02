package com.luipy.utilsmod.config;

/**
 * Persisted mod configuration (JSON). Loaded on both logical server and client.
 */
@SuppressWarnings("CanBeFinal")
public final class LuipyUtilsConfig {
	public static final int HIGHLIGHT_PROFILE_COUNT = 3;
	public static final String DEFAULT_HIGHLIGHT_BLOCK_IDS = "redstone_ore, gravel, clay";

	public boolean masterEnabled = true;
	/** Ender chest panel access mode in the unified menu (Alt+R). */
	public EnderChestAccessMode enderChestAccess = EnderChestAccessMode.BOTH;
	/** When true, show an ender chest icon left of the hotbar when ender access in the unified menu is available. */
	public boolean showEnderGateHudIndicator = true;
	public boolean showToastsOnFailure = true;
	/** When true, hovering an enchantment table option shows ALL enchantments that will be applied. */
	public boolean showEnchantmentPreview = true;
	/** When true, shift+right-clicking a shulker box in your inventory opens it as a container. */
	public boolean allowOpenShulkerFromInventory = true;
	/** When true, the unified menu (Alt+R) includes a 3×3 crafting table panel. */
	public boolean showCraftingTableWithInventory = false;

	// Workstation panels (left column) — all default off (opt-in).
	public WorkstationAccessMode anvilAccess = WorkstationAccessMode.OFF;
	public WorkstationAccessMode smithingAccess = WorkstationAccessMode.OFF;
	public WorkstationAccessMode cartographyAccess = WorkstationAccessMode.OFF;
	public WorkstationAccessMode grindstoneAccess = WorkstationAccessMode.OFF;
	public WorkstationAccessMode stonecutterAccess = WorkstationAccessMode.OFF;
	public WorkstationAccessMode loomAccess = WorkstationAccessMode.OFF;

	/** When true, matching blocks from the active highlight profile are outlined in the world (client-only). */
	public boolean blockHighlightEnabled = false;
	/**
	 * Legacy single-list field; migrated into {@link #blockHighlightProfiles}[0] on load when profiles are empty.
	 * @deprecated use {@link #blockHighlightProfiles}
	 */
	@Deprecated
	public String blockHighlightIds = "";
	/** When true and the global custom PNG exists, use it as fallback after per-profile textures. */
	public boolean blockHighlightUseCustomTexture = false;
	/** Highlight profiles (name, block list, enable switch, optional custom texture). */
	public HighlightProfile[] blockHighlightProfiles;
	/** Index into {@link #blockHighlightProfiles} used for in-world highlighting and cycling. */
	public int activeBlockHighlightProfile = 0;

	public static final class HighlightProfile {
		public String name = "Profile 1";
		public String blockIds = "";
		public boolean enabled = true;
		public boolean useCustomTexture = false;
	}

	public void ensureProfilesInitialized() {
		if (blockHighlightProfiles == null || blockHighlightProfiles.length != HIGHLIGHT_PROFILE_COUNT) {
			HighlightProfile[] fresh = new HighlightProfile[HIGHLIGHT_PROFILE_COUNT];
			for (int i = 0; i < HIGHLIGHT_PROFILE_COUNT; i++) {
				HighlightProfile profile = new HighlightProfile();
				profile.name = "Profile " + (i + 1);
				if (i == 0) {
					profile.blockIds = DEFAULT_HIGHLIGHT_BLOCK_IDS;
				}
				if (blockHighlightProfiles != null && i < blockHighlightProfiles.length && blockHighlightProfiles[i] != null) {
					HighlightProfile existing = blockHighlightProfiles[i];
					profile.name = existing.name != null ? existing.name : profile.name;
					profile.blockIds = existing.blockIds != null ? existing.blockIds : profile.blockIds;
					profile.enabled = existing.enabled;
					profile.useCustomTexture = existing.useCustomTexture;
				}
				fresh[i] = profile;
			}
			blockHighlightProfiles = fresh;
		}
	}

	public HighlightProfile activeHighlightProfile() {
		ensureProfilesInitialized();
		int index = Math.floorMod(activeBlockHighlightProfile, HIGHLIGHT_PROFILE_COUNT);
		return blockHighlightProfiles[index];
	}

	/** Copies legacy {@link #blockHighlightIds} into profile 1 when profiles have no block lists yet. */
	public void migrateLegacyBlockHighlightIds() {
		ensureProfilesInitialized();
		if (blockHighlightIds == null || blockHighlightIds.isBlank()) {
			return;
		}
		boolean profilesEmpty = true;
		for (HighlightProfile profile : blockHighlightProfiles) {
			if (profile.blockIds != null && !profile.blockIds.isBlank()) {
				profilesEmpty = false;
				break;
			}
		}
		if (profilesEmpty) {
			blockHighlightProfiles[0].blockIds = blockHighlightIds.trim();
			blockHighlightIds = "";
		}
	}
}
