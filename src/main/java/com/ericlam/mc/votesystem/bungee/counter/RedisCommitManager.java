package com.ericlam.mc.votesystem.bungee.counter;

import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.votesystem.bungee.VoterConfig;
import com.ericlam.mc.votesystem.bungee.VoterUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RedisCommitManager {


    private void commitSingle(Jedis jedis, UUID player, VoteStats stats) throws JedisException {
        Map<String, String> map = new HashMap<>();
        map.put("vote", stats.getVotes() + "");
        map.put("is-voted-today", stats.isVotedToday() + "");
        jedis.hmset(player.toString(), map);
        VoterUtils.debug("committed " + player + " stats to redis");
    }

    public void commitVote(UUID player, int vote) {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            jedis.hset(player.toString(), "vote", vote + "");
            this.notifyUpdate(jedis, player);
        }
    }

    public void commitVotedToday(UUID player, boolean voted) {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            jedis.hset(player.toString(), "is-voted-today", voted + "");
            this.notifyUpdate(jedis, player);
        }
    }

    public void commitStats(UUID player, VoteStats stats) {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            this.commitSingle(jedis, player, stats);
            this.notifyUpdate(jedis, player);
        }
    }

    public void commitStats(Map<UUID, VoteStats> voteStatsMap) {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            for (UUID uuid : voteStatsMap.keySet()) {
                VoteStats stats = voteStatsMap.get(uuid);
                if (stats.isNotChanged()) continue;
                this.commitSingle(jedis, uuid, stats);
            }
            this.notifyUpdate(jedis);
        }

    }

    public void removeAll(Map<UUID, VoteStats> voteStatsMap) {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            for (UUID uuid : voteStatsMap.keySet()) {
                jedis.hdel(uuid.toString(), "vote", "is-voted-today");
            }
            jedis.del("Vote-Reward-Command");
        }
    }

    public void commitSpigotCommands(VoterConfig configManager) {
        List<String> spigotCommands = configManager.reward.spigotCommands;
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            VoterUtils.debug("uploading reward commands...");
            jedis.del("Vote-Reward-Command");
            for (String s : spigotCommands) {
                jedis.lpush("Vote-Reward-Command", s);
            }
            this.notifyUpdate(jedis);
        }
    }

    public void notifyUpdate() {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            this.notifyUpdate(jedis);

        }
    }

    public void notifyUpdate(UUID player) {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            this.notifyUpdate(jedis, player);
        }
    }


    private void notifyUpdate(Jedis jedis) {
        jedis.publish("Vote-Slave", "UPDATE");
        VoterUtils.debug("notified slave to update their cache.");
    }

    private void notifyUpdate(Jedis jedis, UUID player) {
        jedis.publish("Vote-Slave", "UPDATE_" + player.toString());
        VoterUtils.debug("notified slave to update " + player.toString() + " cache.");
    }

    public void publish(ProxiedPlayer player, int votes) {
        String uuid = player.getUniqueId().toString();
        String server = player.getServer().getInfo().getName();
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            jedis.publish("Vote-" + server, "REWARD_" + uuid + "_" + votes);
            VoterUtils.debug("notified slave to reward " + uuid + " " + votes + " times in server: " + server);
        }
    }


}
