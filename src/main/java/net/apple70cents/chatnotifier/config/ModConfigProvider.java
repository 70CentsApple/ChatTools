package net.apple70cents.chatnotifier.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class ModConfigProvider {
    public ModConfig config;

    public ModConfigProvider(){
        this.config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public ModConfig getConfig(){
        return this.config;
    }
}
