package net.limit.cubliminal;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Comparator;

public interface IniterClient {
	default void init() {}

	static void initialise() {
		FabricLoader.getInstance()
				.getEntrypoints("initerclient", IniterClient.class)
				.forEach(IniterClient::init);
	}
}
