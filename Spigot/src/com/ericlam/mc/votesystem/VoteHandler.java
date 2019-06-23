package com.ericlam.mc.votesystem;

import com.ericlam.mc.votesystem.main.VoterSystemSpigot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class VoteHandler {

    private Plugin plugin;

    public VoteHandler() {
        this.plugin = VoterSystemSpigot.plugin;
    }

    public void reward(UUID uuid, List<String> commands) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player.getName() == null) {
                plugin.getLogger().warning("PlayerName is null, cannot reward.");
                return;
            }
            commands.forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("<player>", player.getName())));
        });
    }
}
