package net.limit.cubliminal;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Initer {
	default void init() {}

	static void initialise() {
		FabricLoader.getInstance()
				.getEntrypoints("initer", Initer.class)
				.forEach(Initer::init); // initialize
	}

	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.TYPE)
	public @interface InitOrder {
		int value() default 0;
	}
}
