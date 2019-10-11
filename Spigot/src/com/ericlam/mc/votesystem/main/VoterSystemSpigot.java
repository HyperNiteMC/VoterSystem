package com.ericlam.mc.votesystem.main;

import com.ericlam.mc.votesystem.VoteDataManager;
import com.ericlam.mc.votesystem.VoteDataPlaceHolder;
import com.ericlam.mc.votesystem.global.RedisManager;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class VoterSystemSpigot extends JavaPlugin {

    public static Plugin plugin;

    private static VoterConfig voterConfig;

    public static void debug(String msg) {
        if (!voterConfig.isDebug()) return;
        plugin.getLogger().info("[DEBUG] " + msg);
    }

    @Override
    public void onLoad() {
        RedisManager.getInstance();
    }

    @Override
    public void onEnable() {
        plugin = this;
        YamlManager yamlManager = HyperNiteMC.getAPI().getFactory().getConfigFactory(this).register("servers.yml", VoterConfig.class).dump();
        voterConfig = yamlManager.getConfigAs(VoterConfig.class);
        String server = voterConfig.getServer();

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            VoteDataManager.getInstance().initializeRedis(this, server);
        });

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().info("Found PlaceHolderAPI, Hooking...");
            new VoteDataPlaceHolder(this, voterConfig).register();
        }

        this.getLogger().info(this.getDescription().getFullName() + " Enabled.");
    }
}
