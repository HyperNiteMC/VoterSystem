package com.ericlam.mc.votesystem.counter;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.votesystem.global.RedisManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RedisCommitManager {
    private static RedisCommitManager redisCommitManager;

    public static RedisCommitManager getInstance() {
        if (redisCommitManager == null) redisCommitManager = new RedisCommitManager();
        return redisCommitManager;
    }

    private RedisCommitManager() {
    }


    private void commitSingle(Jedis jedis, UUID player, VoteStats stats) throws JedisException {
        Map<String, String> map = new HashMap<>();
        map.put("vote",stats.getVotes()+"");
        map.put("is-voted-today",stats.isVotedToday()+"");
        jedis.hmset(player.toString(), map);
    }

    public void commitVote(UUID player, int vote){
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            jedis.hset(player.toString(),"vote",vote+"");
            this.notifyUpdate(jedis, player);
        }
    }

    public void commitVotedToday(UUID player, boolean voted){
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            jedis.hset(player.toString(),"is-voted-today",voted+"");
            this.notifyUpdate(jedis, player);
        }
    }

    public void commitStats(UUID player, VoteStats stats){
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
             this.commitSingle(jedis, player, stats);
            this.notifyUpdate(jedis, player);
        }
    }

    public void commitStats(Map<UUID, VoteStats> voteStatsMap){
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            for (UUID uuid : voteStatsMap.keySet()) {
                VoteStats stats = voteStatsMap.get(uuid);
                if (stats.isNotChanged()) continue;
                this.commitSingle(jedis, uuid, stats);
            }
            this.notifyUpdate(jedis);
        }

    }

    public void removeAll(Map<UUID, VoteStats> voteStatsMap){
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            for (UUID uuid : voteStatsMap.keySet()) {
                jedis.hdel(uuid.toString(),"vote","is-voted-today");
            }
            jedis.del("Vote-Reward-Command");
        }
    }

    public void commitSpigotCommands(ConfigManager configManager){
        List<String> spigotCommands = configManager.getDataList("spi-cmd",String.class);
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            for (String s : spigotCommands) {
                jedis.lpush("Vote-Reward-Command", s);
            }
            this.notifyUpdate(jedis);
        }
    }

    public void notifyUpdate(){
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            jedis.publish("Vote-Slave","UPDATE");
        }
    }

    public void notifyUpdate(UUID player) {
        try (Jedis jedis = RedisManager.getInstance().getRedis()) {
            jedis.publish("Vote-Slave", "UPDATE_" + player.toString());
        }
    }


    private void notifyUpdate(Jedis jedis){
        jedis.publish("Vote-Slave","UPDATE");
    }

    private void notifyUpdate(Jedis jedis, UUID player) {
        jedis.publish("Vote-Slave", "UPDATE_" + player.toString());
    }

    public void publish(ProxiedPlayer player, int votes){
        String uuid = player.getUniqueId().toString();
        String server = player.getServer().getInfo().getName();
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            jedis.publish("Vote-"+server,"REWARD_"+uuid+"_"+votes);
        }
    }





}
