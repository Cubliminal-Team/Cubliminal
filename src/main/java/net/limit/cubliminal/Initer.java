package net.limit.cubliminal;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Comparator;

public interface Initer {
	default void init() {}

	default int order() {
		return 0;
	}

	static void initialise() {
		FabricLoader.getInstance()
				.getEntrypoints("initer", Initer.class)
				.stream()
				.sorted(Comparator.comparingInt(Initer::order)) // sort by order
				.forEach(Initer::init); // initialize
	}
}
