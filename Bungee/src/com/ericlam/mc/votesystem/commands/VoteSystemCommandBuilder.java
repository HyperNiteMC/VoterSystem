package com.ericlam.mc.votesystem.commands;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNode;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNodeBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommand;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommandBuilder;
import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.bungee.hnmc.permission.Perm;
import com.ericlam.mc.votesystem.VoterUtils;
import com.ericlam.mc.votesystem.counter.VoteMySQLManager;
import com.ericlam.mc.votesystem.counter.VoteStats;
import com.ericlam.mc.votesystem.counter.VoteStatsManager;
import com.ericlam.mc.votesystem.listener.Wrapper;
import com.ericlam.mc.votesystem.main.VoterSystemBungee;

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
                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        if (offlinePlayer.isEmpty()) return null;
                        return new Wrapper(VoteMySQLManager.getInstance().getPlayerVote(offlinePlayer.get().getUniqueId()), offlinePlayer.get());
                    }).whenComplete(((wrapper, throwable) -> {
                        if (wrapper == null) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = wrapper.player;
                        VoteStats stats = wrapper.voteStats;
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.votes").replace("<name>", target.getName()).replace("<vote>", stats.getVotes() + ""));
                    }));

                }).build();
        CommandNode vote = new CommandNodeBuilder("vote")
                .description("模擬該玩家的投票")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).whenComplete((offlinePlayer, throwable) -> {
                        if (offlinePlayer.isEmpty() || !offlinePlayer.get().isOnline()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offlinePlayer.get();
                        if (VoterUtils.notInLobby(target.getPlayer())) {
                            MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.player-not-in-lobby"));
                            return;
                        }
                        VoterUtils.reward(target.getPlayer(), 1);
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.simulate"));
                    });

                }).build();
        CommandNode today = new CommandNodeBuilder("today")
                .description("檢查該玩家在24小時內是否有投票")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).whenComplete((offlinePlayer, throwable) -> {
                        if (offlinePlayer.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offlinePlayer.get();
                        boolean voted = VoteStatsManager.getInstance().hasVotedToday(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.is-voted-today").replace("<name>", target.getName()).replace("<var>", voted ? "已經" : "尚未"));
                    });
                }).build();
        CommandNode cancel = new CommandNodeBuilder("cancel")
                .description("解除玩家目前的冷卻狀態。")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).whenComplete((offlinePlayer, throwable) -> {
                        if (offlinePlayer.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }

                        OfflinePlayer target = offlinePlayer.get();
                        if (!VoteStatsManager.getInstance().hasVotedToday(target.getUniqueId())) {
                            MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.already-cancel").replace("<name>", target.getName()));
                            return;
                        }
                        VoteStatsManager.getInstance().removeVotedToday(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.cancel").replace("<name>", target.getName()));
                    });
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

                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        offlinePlayer.ifPresent(player -> VoteMySQLManager.getInstance().getPlayerVote(player.getUniqueId()));
                        return offlinePlayer;
                    }).whenComplete((offline, throwable) -> {
                        if (offline.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offline.get();
                        VoteStatsManager.getInstance().setVotes(target.getUniqueId(), amount);
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.set")
                                .replace("<name>", target.getName())
                                .replace("<vote>", amount + ""));
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
                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        offlinePlayer.ifPresent(player -> VoteMySQLManager.getInstance().getPlayerVote(player.getUniqueId()));
                        return offlinePlayer;
                    }).whenComplete(((offlinePlayer, throwable) -> {
                        if (offlinePlayer.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offlinePlayer.get();
                        VoteStatsManager.getInstance().editVotes(target.getUniqueId(), amount, true);
                        int i = VoteStatsManager.getInstance().getVotes(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.add")
                                .replace("<name>", target.getName())
                                .replace("<amount>", amount + "")
                                .replace("<vote>", i + ""));

                    }));
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

                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        offlinePlayer.ifPresent(player -> VoteMySQLManager.getInstance().getPlayerVote(player.getUniqueId()));
                        return offlinePlayer;
                    }).whenComplete(((offlinePlayer, throwable) -> {
                        if (offlinePlayer.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offlinePlayer.get();
                        if (VoteStatsManager.getInstance().getVotes(target.getUniqueId()) < 1) {
                            MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.zero").replace("<name>", target.getName()));
                            return;
                        }
                        VoteStatsManager.getInstance().editVotes(target.getUniqueId(), amount, false);
                        int i = VoteStatsManager.getInstance().getVotes(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, configManager.getMessage("messages.remove")
                                .replace("<name>", target.getName())
                                .replace("<amount>", amount + "")
                                .replace("<vote>", i + ""));
                    }));
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
