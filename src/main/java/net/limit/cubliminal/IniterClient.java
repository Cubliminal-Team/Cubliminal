package net.limit.cubliminal;

import net.fabricmc.loader.api.FabricLoader;

public interface IniterClient {
	default void init() {}
	static void initialise() {
		FabricLoader.getInstance().getEntrypoints("initerclient", IniterClient.class).forEach(IniterClient::init);
	}
}
