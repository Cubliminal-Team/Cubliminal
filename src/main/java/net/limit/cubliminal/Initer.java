package net.limit.cubliminal;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Comparator;

public interface Initer {
	default void init() {}

	static void initialise() {
		FabricLoader.getInstance()
				.getEntrypoints("initer", Initer.class)
				.forEach(Initer::init); // initialize
	}
}
