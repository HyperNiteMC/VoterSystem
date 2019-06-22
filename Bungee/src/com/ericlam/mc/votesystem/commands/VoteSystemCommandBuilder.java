package com.ericlam.mc.votesystem.commands;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNode;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNodeBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommand;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommandBuilder;
import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.bungee.hnmc.permission.Perm;
import com.ericlam.mc.votesystem.VoterUtils;
import com.ericlam.mc.votesystem.counter.RedisCommitManager;
import com.ericlam.mc.votesystem.counter.VoteMySQLManager;
import com.ericlam.mc.votesystem.counter.VoteStatsManager;
import com.ericlam.mc.votesystem.main.VoterSystemBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletableFuture;

public class VoteSystemCommandBuilder {

    private DefaultCommand defaultCommand;

    public VoteSystemCommandBuilder(){
        ConfigManager configManager = VoterSystemBungee.getConfigManager();
        CommandNode getVote = new CommandNodeBuilder("getvote")
                .description("顯示該玩家擁有的票數")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                    if (target == null){
                        MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                        return;
                    }
                    CompletableFuture.supplyAsync(()-> VoteMySQLManager.getInstance().getPlayerVote(target.getUniqueId()).getVotes()).whenComplete((i,e)->{
                        if (e == null){
                           MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.votes").replace("<name>",target.getName()).replace("<vote>",i+""));
                        }else{
                            e.printStackTrace();
                            MessageBuilder.sendMessage(commandSender, e.getMessage());
                        }
                    });
                }).build();
        CommandNode vote = new CommandNodeBuilder("vote")
                .description("模擬該玩家的投票")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                    if (target == null || !target.isConnected()){
                        MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                        return;
                    }
                    if (!VoterUtils.inLobby(target)){
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.player-not-in-lobby"));
                        return;
                    }
                    VoterUtils.reward(target, 1);
                    MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.simulate"));
                }).build();
        CommandNode today = new CommandNodeBuilder("today")
                .description("檢查該玩家在24小時內是否有投票")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                    if (target == null){
                        MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                        return;
                    }
                    boolean voted = VoteStatsManager.getInstance().hasVotedToday(target.getUniqueId());
                    MessageBuilder.sendMessage(commandSender, configManager.getMessage("is-voted-today").replace("<name>",target.getName()).replace("<var>", voted ? "已經" : "尚未"));
                }).build();
        CommandNode cancel = new CommandNodeBuilder("cancel")
                .description("解除玩家目前的冷卻狀態。")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                    if (target == null){
                        MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                        return;
                    }
                    VoteStatsManager.getInstance().removeVotedToday(target.getUniqueId());
                    MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.cancel").replace("<name>",target.getName()));
                }).build();
        CommandNode set = new CommandNodeBuilder("set")
                .description("設置玩家票數")
                .permission(Perm.ADMIN)
                .placeholder("<player> <amount>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    int amount = this.parseInt(list.get(1));
                    if (amount < 0){
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.not-number"));
                        return;
                    }
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                    if (target == null){
                        MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                        return;
                    }
                    CompletableFuture.runAsync(()->VoteMySQLManager.getInstance().getPlayerVote(target.getUniqueId())).whenComplete((v,e)->{
                        if (e == null){
                            VoteStatsManager.getInstance().setVotes(target.getUniqueId(),amount);
                            MessageBuilder.sendMessage(commandSender, configManager.getMessage("set")
                                    .replace("<name>",target.getName())
                                    .replace("<vote>",amount+""));
                        }else{
                            e.printStackTrace();
                            MessageBuilder.sendMessage(commandSender, e.getMessage());
                        }
                    });
                }).build();
        CommandNode add = new CommandNodeBuilder("add")
                .description("添加玩家票數")
                .permission(Perm.ADMIN)
                .placeholder("<player> <amount>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    int amount = this.parseInt(list.get(1));
                    if (amount < 0){
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.not-number"));
                        return;
                    }
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                    if (target == null){
                        MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                        return;
                    }

                    CompletableFuture.runAsync(()->VoteMySQLManager.getInstance().getPlayerVote(target.getUniqueId())).whenComplete((v,e)->{
                        if (e == null){
                            VoteStatsManager.getInstance().editVotes(target.getUniqueId(), amount, true);
                            int i = VoteStatsManager.getInstance().getVotes(target.getUniqueId());
                            MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.add")
                                    .replace("<name>",target.getName())
                                    .replace("<amount>",amount+"")
                                    .replace("<vote>",i+""));
                        }else{
                            e.printStackTrace();
                            MessageBuilder.sendMessage(commandSender, e.getMessage());
                        }
                    });
                }).build();
        CommandNode remove = new CommandNodeBuilder("remove")
                .description("移除玩家票數")
                .permission(Perm.ADMIN)
                .placeholder("<player> <amount>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    int amount = this.parseInt(list.get(1));
                    if (amount < 0){
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.not-number"));
                        return;
                    }
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                    if (target == null){
                        MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                        return;
                    }

                    CompletableFuture.runAsync(()->VoteMySQLManager.getInstance().getPlayerVote(target.getUniqueId())).whenComplete((v,e)->{
                        if (e == null){
                            VoteStatsManager.getInstance().editVotes(target.getUniqueId(), amount, false);
                            int i = VoteStatsManager.getInstance().getVotes(target.getUniqueId());
                            MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.remove")
                                    .replace("<name>",target.getName())
                                    .replace("<amount>",amount+"")
                                    .replace("<vote>",i+""));
                        }else{
                            e.printStackTrace();
                            MessageBuilder.sendMessage(commandSender, e.getMessage());
                        }
                    });
                }).build();
        CommandNode update = new CommandNodeBuilder("update")
                .alias("refresh")
                .permission(Perm.ADMIN)
                .description("強制更新 Redis 資料")
                .execute((commandSender, list) -> {
                    CompletableFuture.runAsync(()->VoteMySQLManager.getInstance().updateRedis()).whenComplete((v,e)->{
                        if (e == null){
                            MessageBuilder.sendMessage(commandSender, "§a強制更新成功。");
                        }else{
                            e.printStackTrace();
                            MessageBuilder.sendMessage(commandSender, e.getMessage());
                        }
                    });
                }).build();
        this.defaultCommand = new DefaultCommandBuilder("votesys")
                .alias("votesystem","voteadmin")
                .description("管理員專用")
                .permission(Perm.ADMIN)
                .children(getVote, vote, today, cancel, set, add, remove, update)
                .build();
    }

    public DefaultCommand getDefaultCommand() {
        return defaultCommand;
    }

    private int parseInt(String str){
        try{
            return Integer.parseInt(str);
        }catch (NumberFormatException e){
            return -1;
        }
    }
}
