package com.ericlam.mc.votesystem;

public class VoteData {
    private int votes;
    private boolean votedToday;

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
