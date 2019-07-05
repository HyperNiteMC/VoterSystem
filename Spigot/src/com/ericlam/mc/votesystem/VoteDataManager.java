package com.ericlam.mc.votesystem;

import com.ericlam.mc.votesystem.global.RedisManager;
import com.ericlam.mc.votesystem.main.VoterSystemSpigot;
import com.ericlam.mc.votesystem.redis.ChannelListener;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import javax.annotation.Nonnull;
import java.util.*;

public class VoteDataManager {
    private static VoteDataManager voteDataManager;
    private Map<UUID, VoteData> voteDataMap = new HashMap<>();
    private List<String> rewardCommands = new ArrayList<>();


    public static VoteDataManager getInstance() {
        if (voteDataManager == null) voteDataManager = new VoteDataManager();
        return voteDataManager;
    }

    private VoteDataManager() {
    }

    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    public void initializeRedis(Plugin plugin, String server){
        try(Jedis redis = RedisManager.getInstance().getRedis()){
            VoterSystemSpigot.debug("Initializing Redis...");
            this.getSpigotCommand(redis);
            redis.subscribe(new ChannelListener(), "Vote-Slave", "Vote-" + server);
        }catch (JedisException e){
            plugin.getLogger().warning("Cannot connect to jedis server, disabling this plugin");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private void getSpigotCommand(Jedis redis) {
        VoterSystemSpigot.debug("Getting spigot reward commands");
        this.rewardCommands = redis.lrange("Vote-Reward-Command", 0, -1);
        VoterSystemSpigot.debug("Spigot commands: " + this.rewardCommands.toString());
    }


    @Nonnull
    public VoteData getVoteData(UUID player){
        if (voteDataMap.containsKey(player)) return voteDataMap.get(player);
        return this.getVoteDataFromRedis(player);
    }

    @Nonnull
    public VoteData getVoteDataFromRedis(UUID player){
        try (Jedis jedis = RedisManager.getInstance().getRedis()) {
            return this.getVoteDataFromRedis(jedis, player);
        }
    }

    @Nonnull
    private VoteData getVoteDataFromRedis(Jedis jedis, UUID player) {
        VoteData voteData;
        VoterSystemSpigot.debug("Getting " + player.toString() + " data from redis");
        List<String> list = jedis.hmget(player.toString(), "vote", "is-voted-today");
        if (list.size() != 2) voteData = new VoteData(0, false);
        else {
            boolean voted = Boolean.parseBoolean(list.get(1));
            int vote;
            try {
                vote = Integer.parseInt(list.get(0));
            } catch (NumberFormatException e) {
                vote = 0;
            }

            voteData = new VoteData(vote, voted);
        }
        voteDataMap.put(player, voteData);
        VoterSystemSpigot.debug("Getting completed.");
        return voteData;
    }

    public void getVoteDataFromRedis() {
        try (Jedis jedis = RedisManager.getInstance().getRedis()) {
            for (UUID player : voteDataMap.keySet()) {
                this.getVoteDataFromRedis(jedis, player);
            }
            this.getSpigotCommand(jedis);
        }
    }

}
