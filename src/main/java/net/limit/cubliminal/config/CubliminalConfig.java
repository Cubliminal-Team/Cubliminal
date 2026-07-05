package net.limit.cubliminal.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "cubliminal")
public class CubliminalConfig implements ConfigData {

	public boolean disableCustomShaders = false;
	public boolean enableSuperDenseFog = true;

	public static CubliminalConfig get() {
        return AutoConfig.getConfigHolder(CubliminalConfig.class).getConfig();
	}
}
