package com.ericlam.mc.votesystem.bungee;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.votesystem.bungee.counter.RedisCommitManager;
import com.ericlam.mc.votesystem.bungee.main.VoterSystemBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VoterUtils {

    public static List<ProxiedPlayer> getWhiteListPlayers() {
        List<String> whitelisted = VoterSystemBungee.voterConfig.whitelistServers;
        List<ServerInfo> servers = ProxyServer.getInstance().getServers().keySet().stream().filter(e -> whitelisted.stream().anyMatch(e::matches)).map(name -> ProxyServer.getInstance().getServerInfo(name)).collect(Collectors.toList());
        List<ProxiedPlayer> players = new ArrayList<>();
        for (ServerInfo server : servers) {
            players.addAll(server.getPlayers());
        }
        return players;
    }

    public static boolean notInLobby(ProxiedPlayer voter) {
        List<String> whitelist = VoterSystemBungee.voterConfig.whitelistServers;
        return whitelist.stream().noneMatch(list -> voter.getServer().getInfo().getName().matches(list));
    }

    public static boolean isLobby(ServerInfo server) {
        List<String> whitelist = VoterSystemBungee.voterConfig.whitelistServers;
        return whitelist.stream().anyMatch(l -> l.matches(server.getName()));
    }

    public static void log(String msg) {
        VoterSystemBungee.INSTANCE.getLogger().info(msg);
    }

    public static void debug(String msg) {
        if (!VoterSystemBungee.voterConfig.debug) return;
        log(msg);
    }

    public static void reward(ProxiedPlayer voter, int votes) {
        VoterUtils.debug("rewarding " + voter.getName());
        VoterConfig configManager = VoterSystemBungee.voterConfig;
        RedisCommitManager redisCommitManager = VoterSystemBungee.INSTANCE.getRedisCommitManager();
        boolean broadcast = configManager.reward.broadcast;
        if (broadcast)
            VoterUtils.getWhiteListPlayers().forEach(p -> MessageBuilder.sendMessage(p, configManager.get("reward.broadcast-message").replace("<player>", voter.getName())));
        new MessageBuilder(configManager.getList("reward.messages").toArray(String[]::new)).sendPlayer(voter);
        redisCommitManager.publish(voter, votes);
    }

    public static void runAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(VoterSystemBungee.INSTANCE, runnable);
    }
}
