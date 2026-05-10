package com.luipy.utilsmod.config;

import com.google.gson.annotations.SerializedName;

/**
 * Persisted mod configuration (JSON). Loaded on both logical server and client.
 */
@SuppressWarnings("CanBeFinal")
public final class LuipyUtilsConfig {
	public boolean masterEnabled = true;
	/** When true, pressing inventory (E) opens survival inventory and ender chest together (requires mod on server). */
	@SerializedName(value = "showEnderChestWithInventory", alternate = { "replaceInventoryWithEnderChest" })
	public boolean showEnderChestWithInventory = true;
	/** When true, item/block gates below are ignored for opening (server still validates). */
	public boolean alwaysAllowVirtualOpen = false;
	/** When {@link #alwaysAllowVirtualOpen} is false: require holding an ender chest item (OR with block). */
	public boolean requireEnderChestItem = true;
	/** When {@link #alwaysAllowVirtualOpen} is false: require a loaded ender chest block nearby (OR with item). */
	public boolean requireNearbyEnderChestBlock = true;
	/** How far to search for a loaded ender chest block (Chebyshev / max axis distance). */
	public int nearbySearchRadiusBlocks = 48;
	/**
	 * When the server does not run this mod: after opening survival inventory, try a vanilla use-block on the
	 * nearest loaded ender chest so the server opens the normal ender GUI (not merged with inventory; that still
	 * requires the mod on the server).
	 */
	public boolean tryOpenNearestEnderOnVanillaServer = true;
	public boolean showToastsOnFailure = true;
}
