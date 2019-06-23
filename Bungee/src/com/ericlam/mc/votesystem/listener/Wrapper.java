package com.ericlam.mc.votesystem.listener;

import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.votesystem.counter.VoteStats;

public class Wrapper {
    public VoteStats voteStats;
    public OfflinePlayer player;

    public Wrapper(VoteStats voteStats, OfflinePlayer player) {
        this.voteStats = voteStats;
        this.player = player;
    }
}
