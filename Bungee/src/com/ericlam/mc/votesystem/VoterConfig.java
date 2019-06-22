package com.ericlam.mc.votesystem;

import com.ericlam.mc.bungee.hnmc.config.ConfigSetter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoterConfig extends ConfigSetter {

    private int announceInterval;
    private boolean announceOnJoin, rewardBroadcast;
    private List<String> spigotCommands;
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

    @Override
    public Map<String, Object> variablesMap() {
        return Map.of(
                "ai", announceInterval,
                "aoj", announceOnJoin,
                "rbc", rewardBroadcast,
                "spi-cmd", spigotCommands,
                "whitelist", whitelistedServers
        );
    }
}
