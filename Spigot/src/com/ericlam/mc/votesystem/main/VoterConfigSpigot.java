package com.ericlam.mc.votesystem.main;

import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import com.hypernite.mc.hnmc.core.config.Extract;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class VoterConfigSpigot extends ConfigSetter {

    @Extract
    private String server;

    VoterConfigSpigot(Plugin plugin) {
        super(plugin, "server.yml");
    }

    @Override
    public void loadConfig(Map<String, FileConfiguration> map) {
        this.server = map.get("server.yml").getString("server");
    }
}
