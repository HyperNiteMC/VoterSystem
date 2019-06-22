package com.ericlam.mc.votesystem.main;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandRegister;
import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.votesystem.VoterConfig;
import com.ericlam.mc.votesystem.VoterUtils;
import com.ericlam.mc.votesystem.commands.VoteCommand;
import com.ericlam.mc.votesystem.commands.VoteSystemCommandBuilder;
import com.ericlam.mc.votesystem.counter.RedisCommitManager;
import com.ericlam.mc.votesystem.counter.VoteMySQLManager;
import com.ericlam.mc.votesystem.counter.VoteStatsManager;
import com.ericlam.mc.votesystem.listener.VotingListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class VoterSystemBungee extends Plugin {

    private static VoterSystemBungee instance;
    private static ConfigManager configManager;

    public static VoterSystemBungee getInstance() {
        return instance;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            configManager = HyperNiteMC.getAPI().registerConfig(new VoterConfig(this));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        configManager.setMsgConfig("config.yml","messages.prefix");
        VoterUtils.runAsync(()->{
            VoteMySQLManager.getInstance().createDatabase();
            RedisCommitManager.getInstance().commitSpigotCommands(configManager);
        });

        CommandRegister commandRegister = HyperNiteMC.getAPI().getCommandRegister();
        commandRegister.registerCommand(this, new VoteCommand());
        commandRegister.registerCommand(this, new VoteSystemCommandBuilder().getDefaultCommand());

        this.getProxy().getPluginManager().registerListener(this, new VotingListener());

        this.launchAnnouncement();
    }

    @Override
    public void onDisable() {
        VoteMySQLManager.getInstance().saveVote();
    }

    private void launchAnnouncement(){
        int interval = configManager.getData("ai",Integer.class).orElse(300);
        ProxyServer.getInstance().getScheduler().schedule(VoterSystemBungee.getInstance(),()->{
            for (String line : configManager.getMessageList("announcement.messages", true)) {
                VoterUtils.getWhiteListPlayers().forEach(p-> MessageBuilder.sendMessage(p,line.replace("<vote>", VoteStatsManager.getInstance().getVotes(p.getUniqueId()) +"")));
            }
        }, interval, interval, TimeUnit.SECONDS);
    }
}
