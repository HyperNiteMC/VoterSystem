package com.ericlam.mc.votesystem.bungee.commands;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNode;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNodeBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommand;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommandBuilder;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.bungee.hnmc.permission.Perm;
import com.ericlam.mc.votesystem.bungee.VoterUtils;
import com.ericlam.mc.votesystem.bungee.counter.RedisCommitManager;
import com.ericlam.mc.votesystem.bungee.counter.VoteMySQLManager;
import com.ericlam.mc.votesystem.bungee.counter.VoteStats;
import com.ericlam.mc.votesystem.bungee.counter.VoteStatsManager;
import com.ericlam.mc.votesystem.bungee.listener.Wrapper;
import com.ericlam.mc.votesystem.bungee.main.VoterSystemBungee;

import java.util.concurrent.CompletableFuture;

public class VoteSystemCommandBuilder {

    private DefaultCommand defaultCommand;

    public VoteSystemCommandBuilder(VoterSystemBungee bungee) {
        var msg = VoterSystemBungee.voterConfig;
        VoteStatsManager statsManager = bungee.getVoteStatsManager();
        RedisCommitManager commitManager = bungee.getRedisCommitManager();
        VoteMySQLManager mySQLManager = bungee.getVoteMySQLManager();
        CommandNode getVote = new CommandNodeBuilder("getvote")
                .description("顯示該玩家擁有的票數")
                .permission(Perm.ADMIN)
                .placeholder("<player>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        if (offlinePlayer.isEmpty()) return null;
                        return new Wrapper(mySQLManager.getPlayerVote(offlinePlayer.get().getUniqueId()), offlinePlayer.get());
                    }).whenComplete(((wrapper, throwable) -> {
                        if (wrapper == null) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = wrapper.player;
                        VoteStats stats = wrapper.voteStats;
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("votes").replace("<name>", target.getName()).replace("<vote>", stats.getVotes() + ""));
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
                            MessageBuilder.sendMessage(commandSender, msg.getMessage("player-not-in-lobby"));
                            return;
                        }
                        VoterUtils.reward(target.getPlayer(), 1);
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("simulate"));
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
                        boolean voted = statsManager.hasVotedToday(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("is-voted-today").replace("<name>", target.getName()).replace("<var>", voted ? "已經" : "尚未"));
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
                        if (!statsManager.hasVotedToday(target.getUniqueId())) {
                            MessageBuilder.sendMessage(commandSender, msg.getMessage("already-cancel").replace("<name>", target.getName()));
                            return;
                        }
                        statsManager.removeVotedToday(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("cancel").replace("<name>", target.getName()));
                    });
                }).build();
        CommandNode set = new CommandNodeBuilder("set")
                .description("設置玩家票數")
                .permission(Perm.ADMIN)
                .placeholder("<player> <amount>")
                .execute((commandSender, list) -> {
                    String name = list.get(0);
                    int amount = this.parseInt(list.get(1));
                    if (amount < 0) {
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("not-number"));
                        return;
                    }

                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        offlinePlayer.ifPresent(player -> mySQLManager.getPlayerVote(player.getUniqueId()));
                        return offlinePlayer;
                    }).whenComplete((offline, throwable) -> {
                        if (offline.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offline.get();
                        statsManager.setVotes(target.getUniqueId(), amount);
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("set")
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
                    if (amount < 0) {
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("not-number"));
                        return;
                    }
                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        offlinePlayer.ifPresent(player -> mySQLManager.getPlayerVote(player.getUniqueId()));
                        return offlinePlayer;
                    }).whenComplete(((offlinePlayer, throwable) -> {
                        if (offlinePlayer.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getPrefix() + HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offlinePlayer.get();
                        statsManager.editVotes(target.getUniqueId(), amount, true);
                        int i = statsManager.getVotes(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("add")
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
                    if (amount < 0) {
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("not-number"));
                        return;
                    }

                    HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenApplyAsync(offlinePlayer -> {
                        offlinePlayer.ifPresent(player -> mySQLManager.getPlayerVote(player.getUniqueId()));
                        return offlinePlayer;
                    }).whenComplete(((offlinePlayer, throwable) -> {
                        if (offlinePlayer.isEmpty()) {
                            MessageBuilder.sendMessage(commandSender, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
                            return;
                        }
                        OfflinePlayer target = offlinePlayer.get();
                        if (statsManager.getVotes(target.getUniqueId()) < 1) {
                            MessageBuilder.sendMessage(commandSender, msg.getMessage("zero").replace("<name>", target.getName()));
                            return;
                        }
                        statsManager.editVotes(target.getUniqueId(), amount, false);
                        int i = statsManager.getVotes(target.getUniqueId());
                        MessageBuilder.sendMessage(commandSender, msg.getMessage("remove")
                                .replace("<name>", target.getName())
                                .replace("<amount>", amount + "")
                                .replace("<vote>", i + ""));
                    }));
                }).build();
        CommandNode update = new CommandNodeBuilder("update")
                .alias("refresh")
                .permission(Perm.ADMIN)
                .description("強制更新 Redis 資料")
                .execute((commandSender, list) -> CompletableFuture.runAsync(() -> mySQLManager.updateRedis()).whenComplete((v, e) -> {
                    if (e == null) {
                        MessageBuilder.sendMessage(commandSender, "§a強制更新成功。");
                    } else {
                        e.printStackTrace();
                        MessageBuilder.sendMessage(commandSender, e.getMessage());
                    }
                })).build();
        this.defaultCommand = new DefaultCommandBuilder("votesys")
                .alias("votesystem", "voteadmin")
                .description("管理員專用")
                .permission(Perm.ADMIN)
                .children(getVote, vote, today, cancel, set, add, remove, update)
                .build();
    }

    public DefaultCommand getDefaultCommand() {
        return defaultCommand;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
