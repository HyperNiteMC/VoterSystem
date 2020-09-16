package com.ericlam.mc.votesystem.spigot;


import com.ericlam.mc.votesystem.spigot.main.VoterSystemSpigot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class VoteHandler {

    private final Plugin plugin;

    public VoteHandler() {
        this.plugin = VoterSystemSpigot.INSTANCE;
    }

    public void reward(UUID uuid, List<String> commands) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player.getName() == null) {
                plugin.getLogger().warning("PlayerName is null, cannot reward.");
                return;
            }
            commands.forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("<player>", player.getName())));
            VoterSystemSpigot.debug("Successfully executed commands to " + player.getName());
        });
    }
}
