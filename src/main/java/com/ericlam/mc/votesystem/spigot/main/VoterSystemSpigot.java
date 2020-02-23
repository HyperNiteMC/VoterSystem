package com.ericlam.mc.votesystem.spigot.main;

import com.ericlam.mc.votesystem.spigot.VoteDataManager;
import com.ericlam.mc.votesystem.spigot.VoteDataPlaceHolder;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class VoterSystemSpigot extends JavaPlugin {

    public static Plugin INSTANCE;

    private static VoterConfig voterConfig;

    private VoteDataManager voteDataManager;

    public static void debug(String msg) {
        if (!voterConfig.isDebug()) return;
        INSTANCE.getLogger().info("[DEBUG] " + msg);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        YamlManager yamlManager = HyperNiteMC.getAPI().getFactory().getConfigFactory(this).register("server.yml", VoterConfig.class).dump();
        voterConfig = yamlManager.getConfigAs(VoterConfig.class);
        String server = voterConfig.getServer();
        voteDataManager = new VoteDataManager();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            voteDataManager.initializeRedis(this, server);
        });

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().info("Found PlaceHolderAPI, Hooking...");
            new VoteDataPlaceHolder(this, voterConfig, voteDataManager).register();
        }
        this.getLogger().info(this.getDescription().getFullName() + " Enabled.");
    }
}
