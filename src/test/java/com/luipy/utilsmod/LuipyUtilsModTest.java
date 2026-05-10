package com.luipy.utilsmod;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LuipyUtilsModTest {
	@Test
	void modIdIsStable() {
		assertEquals("luipy-utils-mod", LuipyUtilsMod.MOD_ID);
	}
}
