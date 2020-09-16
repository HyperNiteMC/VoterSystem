package com.ericlam.mc.votesystem.spigot;

public class VoteData {
    private final int votes;
    private final boolean votedToday;

    public VoteData(int votes, boolean votedToday) {
        this.votes = votes;
        this.votedToday = votedToday;
    }

    public int getVotes() {
        return votes;
    }

    public boolean isVotedToday() {
        return votedToday;
    }
}
