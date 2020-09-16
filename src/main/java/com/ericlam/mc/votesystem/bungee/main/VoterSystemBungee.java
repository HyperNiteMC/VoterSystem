package com.ericlam.mc.votesystem.bungee.main;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandRegister;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.votesystem.bungee.VoterConfig;
import com.ericlam.mc.votesystem.bungee.VoterUtils;
import com.ericlam.mc.votesystem.bungee.commands.VoteCommand;
import com.ericlam.mc.votesystem.bungee.commands.VoteSystemCommandBuilder;
import com.ericlam.mc.votesystem.bungee.counter.RedisCommitManager;
import com.ericlam.mc.votesystem.bungee.counter.VoteMySQLManager;
import com.ericlam.mc.votesystem.bungee.counter.VoteStatsManager;
import com.ericlam.mc.votesystem.bungee.listener.VotingListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class VoterSystemBungee extends Plugin {

    public static VoterSystemBungee INSTANCE;
    public static VoterConfig voterConfig;

    private RedisCommitManager redisCommitManager;
    private VoteMySQLManager voteMySQLManager;
    private VoteStatsManager voteStatsManager;

    private static void launchAnnouncement() {
        int interval = voterConfig.announcement.interval;
        ProxyServer.getInstance().getScheduler().schedule(INSTANCE, () -> sendAnnouncement(VoterUtils.getWhiteListPlayers()), interval, interval, TimeUnit.SECONDS);
    }

    public static void sendAnnouncement(List<ProxiedPlayer> players) {
        for (String line : voterConfig.getList("announcement.messages")) {
            players.forEach(p -> MessageBuilder.sendMessage(p, line.replace("<vote>", VoterSystemBungee.INSTANCE.voteStatsManager.getVotes(p.getUniqueId()) + "")));
        }
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        var manager = HyperNiteMC.getAPI().getConfigFactory(this).register(VoterConfig.class).dump();
        voterConfig = manager.getConfigAs(VoterConfig.class);
        redisCommitManager = new RedisCommitManager();
        voteStatsManager = new VoteStatsManager(redisCommitManager);
        voteMySQLManager = new VoteMySQLManager(voteStatsManager, redisCommitManager);
        VoterUtils.runAsync(() -> {
            voteMySQLManager.createDatabase();
            redisCommitManager.commitSpigotCommands(voterConfig);
        });
        CommandRegister commandRegister = HyperNiteMC.getAPI().getCommandRegister();
        commandRegister.registerCommand(this, new VoteCommand());
        commandRegister.registerCommand(this, new VoteSystemCommandBuilder(this).getDefaultCommand());
        this.getProxy().getPluginManager().registerListener(this, new VotingListener(voteStatsManager, voteMySQLManager));

        launchAnnouncement();

        this.getLogger().info("VoteSystem Enabled.");
    }

    public RedisCommitManager getRedisCommitManager() {
        return redisCommitManager;
    }

    public VoteStatsManager getVoteStatsManager() {
        return voteStatsManager;
    }

    public VoteMySQLManager getVoteMySQLManager() {
        return voteMySQLManager;
    }

    @Override
    public void onDisable() {
        voteMySQLManager.saveVote();
    }
}
