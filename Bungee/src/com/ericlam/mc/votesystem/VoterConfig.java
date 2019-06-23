package com.ericlam.mc.votesystem;

import com.ericlam.mc.bungee.hnmc.config.ConfigSetter;
import com.ericlam.mc.bungee.hnmc.config.Extract;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.List;

public class VoterConfig extends ConfigSetter {

    @Extract(name = "ai")
    private int announceInterval;

    @Extract(name = "aoj")
    private boolean announceOnJoin;

    @Extract(name = "rbc")
    private boolean rewardBroadcast;

    @Extract(name = "spi-cmd")
    private List<String> spigotCommands;

    @Extract(name = "whitelist")
    private List<String> whitelistedServers;

    public VoterConfig(Plugin plugin) {
        super(plugin, "config.yml");
    }

    @Override
    public void loadConfig(HashMap<String, Configuration> hashMap) {
        Configuration config = hashMap.get("config.yml");
        this.announceInterval = config.getInt("announcement.interval");
        this.announceOnJoin = config.getBoolean("announcement.on-join");
        this.rewardBroadcast = config.getBoolean("reward.broadcast");
        this.spigotCommands = config.getStringList("reward.spigot-commands");
        this.whitelistedServers = config.getStringList("whitelist-servers");
    }
}
