package net.limit.cubliminal;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Comparator;

public interface IniterClient {
	default void init() {}

	default int order() {
		return 0;
	}

	static void initialise() {
		FabricLoader.getInstance()
				.getEntrypoints("initerclient", IniterClient.class)
				.stream()
				.sorted(Comparator.comparingInt(IniterClient::order))
				.forEach(IniterClient::init);
	}
}
