package com.luipy.utilsmod.client;

public final class LuipyClientState {
	private static volatile boolean serverAnnouncedMod;

	public static void setServerHasLuipyMod(boolean value) {
		serverAnnouncedMod = value;
	}

	public static boolean serverHasLuipyMod() {
		return serverAnnouncedMod;
	}
}
