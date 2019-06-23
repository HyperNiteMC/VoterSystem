package com.ericlam.mc.votesystem.redis;

import com.ericlam.mc.votesystem.VoteDataManager;
import com.ericlam.mc.votesystem.VoteHandler;
import com.ericlam.mc.votesystem.main.VoterSystemSpigot;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.UUID;

public class ChannelListener extends JedisPubSub {

    private String server;
    private VoteDataManager voteDataManager;
    private VoteHandler voteHandler;

    public ChannelListener(String server) {
        this.server = server;
        this.voteDataManager = VoteDataManager.getInstance();
        this.voteHandler = new VoteHandler();
    }

    @Override
    public void onMessage(String channel, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(VoterSystemSpigot.plugin, () -> {
            if (!channel.equalsIgnoreCase("Vote-Master") && !channel.equalsIgnoreCase("Vote-" + server)) return;
            String[] params = message.split("_");
            String method = params[0].toLowerCase();
            String[] args = Arrays.copyOfRange(params, 1, params.length);
            switch (method) {
                case "update":
                    if (args.length < 1) {
                        voteDataManager.getVoteDataFromRedis();
                    } else {
                        UUID uuid = UUID.fromString(args[0]);
                        voteDataManager.getVoteDataFromRedis(uuid);
                    }
                    break;
                case "reward":
                    if (args.length < 2) break;
                    UUID player = UUID.fromString(args[0]);
                    int votes;
                    try {
                        votes = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        break;
                    }
                    for (int i = 0; i < votes; i++) {
                        voteHandler.reward(player, voteDataManager.getRewardCommands());
                    }
                    break;
                default:
                    break;
            }
        });
    }
}
