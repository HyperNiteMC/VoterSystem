package com.ericlam.mc.votesystem.main;

import com.ericlam.mc.votesystem.VoteDataManager;
import com.ericlam.mc.votesystem.VoteDataPlaceHolder;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class VoterSystemSpigot extends JavaPlugin {

    private static ConfigManager configManager;
    public static Plugin plugin;


    public static ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {
        plugin = this;
        configManager = HyperNiteMC.getAPI().registerConfig(new VoterConfigSpigot(this));
        configManager.setMsgConfig("server.yml");
        String server = configManager.getData("server", String.class).orElse("Unknown");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> VoteDataManager.getInstance().initializeRedis(this, server));

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().info("Found PlaceHolderAPI, Hooking...");
            new VoteDataPlaceHolder(this).register();
        }

        this.getLogger().info(this.getDescription().getFullName() + " Enabled.");
    }
}
