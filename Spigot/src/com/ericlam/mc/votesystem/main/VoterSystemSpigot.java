package com.ericlam.mc.votesystem.main;

import com.ericlam.mc.votesystem.VoteDataManager;
import com.ericlam.mc.votesystem.VoteDataPlaceHolder;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class VoterSystemSpigot extends JavaPlugin {

    public static Plugin plugin;

    private static ConfigManager configManager;

    public static void debug(String msg) {
        if (!configManager.getData("debug", Boolean.class).orElse(false)) return;
        plugin.getLogger().info("[DEBUG] " + msg);
    }

    @Override
    public void onEnable() {
        plugin = this;
        configManager = HyperNiteMC.getAPI().registerConfig(new VoterConfigSpigot(this));
        configManager.setMsgConfig("server.yml");
        String server = configManager.getData("server", String.class).orElse("Unknown");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            VoteDataManager.getInstance().initializeRedis(this, server);
        });

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().info("Found PlaceHolderAPI, Hooking...");
            new VoteDataPlaceHolder(this, configManager).register();
        }

        this.getLogger().info(this.getDescription().getFullName() + " Enabled.");
    }
}
