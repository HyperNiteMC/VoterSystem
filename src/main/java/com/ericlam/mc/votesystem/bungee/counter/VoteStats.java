package com.ericlam.mc.votesystem.bungee.counter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class VoteStats {
    private int Votes;
    private long TimeStamp;
    private boolean isVotedToday;
    private int queueVote;
    private boolean isChanged;

    public VoteStats(int votes, long timeStamp, int queueVote) {
        Votes = votes;
        TimeStamp = timeStamp;
        this.queueVote = queueVote;
        isVotedToday = (Timestamp.from(Instant.now()).getTime() - TimeStamp) < TimeUnit.DAYS.toMillis(1);
        isChanged = false;
    }

    public int getVotes() {
        return Votes;
    }

    public void setVotes(int votes) {
        Votes = votes < 0 ? 0 : votes;
        isChanged = true;
    }

    public long getTimeStamp() {
        if (TimeStamp < 0) TimeStamp = 0;
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
        isVotedToday = (Timestamp.from(Instant.now()).getTime() - timeStamp) < TimeUnit.DAYS.toMillis(1);
        isChanged = true;
    }

    public boolean isVotedToday() {
        return isVotedToday;
    }

    public void setVotedToday(boolean votedToday) {
        this.isVotedToday = votedToday;
    }

    public int getQueueVote() {
        if (queueVote < 0) queueVote = 0;
        return queueVote;
    }

    public void setQueueVote(int queueVote) {
        this.queueVote = queueVote;
        isChanged = true;
    }

    public boolean isNotChanged() {
        return !isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;

    }
}
