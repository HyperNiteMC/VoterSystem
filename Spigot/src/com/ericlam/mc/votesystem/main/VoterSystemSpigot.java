package com.ericlam.mc.votesystem.main;

import com.ericlam.mc.votesystem.VoteDataManager;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class VoterSystemSpigot extends JavaPlugin {

    private static ConfigManager configManager;
    public static Plugin plugin;


    @Override
    public void onEnable() {
        plugin = this;
        configManager = HyperNiteMC.getAPI().registerConfig(new VoterConfigSpigot(this));
        String server = configManager.getData("server", String.class).orElse("Unknown");

        VoteDataManager.getInstance().initializeRedis(this, server);
    }
}
