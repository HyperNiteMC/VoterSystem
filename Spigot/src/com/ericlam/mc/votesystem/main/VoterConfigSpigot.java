package com.ericlam.mc.votesystem.main;

import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class VoterConfigSpigot extends ConfigSetter {

    private String server;

    public VoterConfigSpigot(Plugin plugin) {
        super(plugin, "server.yml");
    }

    @Override
    public void loadConfig(Map<String, FileConfiguration> map) {
        this.server = map.get("server.yml").getString("server");
    }


    @Override
    public Map<String, Object> variablesMap() {
        return Map.of("server", this.server);
    }
}
