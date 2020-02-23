package com.ericlam.mc.votesystem.bungee.counter;

import com.ericlam.mc.votesystem.bungee.VoterUtils;
import com.ericlam.mc.votesystem.bungee.main.VoterSystemBungee;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VoteStatsManager {
    private HashMap<UUID, VoteStats> stats = new HashMap<>();

    /*
        Map Handle
     */

    private final RedisCommitManager redisCommitManager;

    public VoteStatsManager() {
        this.redisCommitManager = VoterSystemBungee.INSTANCE.getRedisCommitManager();
    }

    void putVote(UUID player, VoteStats vote) {
        stats.put(player, vote);
    }

    void removeVote(UUID player) {
        stats.remove(player);
    }

    boolean containVote(UUID player) {
        return stats.containsKey(player);
    }

    VoteStats getVote(UUID player) {
        return stats.get(player);
    }

    HashMap<UUID, VoteStats> getStats() {
        return stats;
    }

    /*
     * Setter
     */


    public void editVotes(UUID player, int amount, boolean add) {
        if ((!stats.containsKey(player) || stats.get(player).getVotes() <= 0) && !add) return;
        if (!stats.containsKey(player) && add) {
            this.setVotes(player, amount);
            return;
        }
        int vote = stats.get(player).getVotes();

        if (add) vote += amount;
        else vote -= amount;

        this.setVotes(player, vote);
    }

    // With redis commit
    public void setVotes(UUID player, int amount) {
        stats.get(player).setVotes(amount);
        VoterUtils.runAsync(() -> redisCommitManager.commitStats(player, stats.get(player)));
    }

    public void setTimeStamp(UUID player, long timestamp) {
        stats.get(player).setTimeStamp(timestamp);
    }

    public void removeVotedToday(UUID player) {
        stats.get(player).setVotedToday(false);
        VoterUtils.runAsync(() -> redisCommitManager.commitVotedToday(player, false));
    }

    public void clearQueueVote(UUID player) {
        stats.get(player).setQueueVote(0);
    }

    /*
     * Getter
     */

    public int getVotes(UUID player) {
        if (!stats.containsKey(player)) return 0;
        else if (stats.get(player).getVotes() < 0) this.setVotes(player, 0);
        return stats.get(player).getVotes();
    }

    public int getVotersCount() {
        return stats.size();
    }


    public int getQueuedVote(UUID player) {
        return stats.get(player).getQueueVote();
    }

    /* Should give to leadersystem handle
    public String getHighestVoter() {
        UUID highest = null;
        for (UUID player : stats.keySet()) {
            if (highest == null) {
                highest = player;
                continue;
            }
            if (stats.get(player).getVotes() > stats.get(highest).getVotes()) {
                highest = player;
            }
        }

        if (highest == null) return "§cNOTHING";

        return ProxyServer.getInstance().getPlayer(highest).getName();
    }
    */
    public long getTimestamp(UUID uuid) {
        return stats.get(uuid).getTimeStamp();
    }

    /*
     * Checker
     */

    public boolean hasVotes(UUID player) {
        return getVotes(player) > 0;
    }

    public boolean hasVotedToday(UUID player) {
        return stats.get(player).isVotedToday();
    }

    public boolean overVoted(UUID player, int requiredAmount) {
        return getVotes(player) >= requiredAmount;
    }

    /*
     * Others ?
     */

    public boolean checkTimeStamp(UUID player, long timestamp) {
        if (!this.hasVotedToday(player)) return true;
        if (timestamp - stats.get(player).getTimeStamp() < TimeUnit.DAYS.toMillis(1)) return false;
        stats.get(player).setVotedToday(false);
        return true;
    }


}
