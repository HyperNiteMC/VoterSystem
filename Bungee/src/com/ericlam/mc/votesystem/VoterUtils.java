package com.ericlam.mc.votesystem;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.votesystem.counter.RedisCommitManager;
import com.ericlam.mc.votesystem.main.VoterSystemBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VoterUtils {

    public static List<ProxiedPlayer> getWhiteListPlayers(){
        List<String> whitelisted = VoterSystemBungee.getConfigManager().getDataList("whitelist",String.class);
        List<ServerInfo> servers =  ProxyServer.getInstance().getServers().keySet().stream().filter(e->whitelisted.stream().anyMatch(e::matches)).map(name->ProxyServer.getInstance().getServerInfo(name)).collect(Collectors.toList());
        List<ProxiedPlayer> players = new ArrayList<>();
        for (ServerInfo server : servers) {
            players.addAll(server.getPlayers());
        }
        return players;
    }

    public static boolean notInLobby(ProxiedPlayer voter) {
        List<String> whitelist = VoterSystemBungee.getConfigManager().getDataList("whitelist",String.class);
        return whitelist.stream().noneMatch(list -> voter.getServer().getInfo().getName().matches(list));
    }

    public static boolean isLobby(ServerInfo server){
        List<String> whitelist = VoterSystemBungee.getConfigManager().getDataList("whitelist",String.class);
        return whitelist.stream().anyMatch(l->l.matches(server.getName()));
    }

    public static void log(String msg){
        VoterSystemBungee.getInstance().getLogger().info(msg);
    }

    public static void debug(String msg) {
        if (!VoterSystemBungee.getConfigManager().getData("debug", Boolean.class).orElse(false)) return;
        log(msg);
    }

    public static void warn(String msg){
        VoterSystemBungee.getInstance().getLogger().info(msg);
    }

    public static void reward(ProxiedPlayer voter,int votes){
        VoterUtils.debug("rewarding " + voter.getName());
        ConfigManager configManager = VoterSystemBungee.getConfigManager();
        RedisCommitManager redisCommitManager = RedisCommitManager.getInstance();
        boolean broadcast = configManager.getData("rbc",Boolean.class).orElse(false);
        if (broadcast)
            VoterUtils.getWhiteListPlayers().forEach(p -> MessageBuilder.sendMessage(p, configManager.getMessage("reward.broadcast-message").replace("<player>", voter.getName())));
        new MessageBuilder(configManager.getMessageList("reward.messages",true)).sendPlayer(voter);
        redisCommitManager.publish(voter, votes);
    }

    public static void runAsync(Runnable runnable){
        ProxyServer.getInstance().getScheduler().runAsync(VoterSystemBungee.getInstance(),runnable);
    }
}
