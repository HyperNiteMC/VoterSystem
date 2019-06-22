package com.ericlam.mc.votesystem.listener;

import com.ericlam.mc.votesystem.VoterUtils;
import com.ericlam.mc.votesystem.counter.VoteMySQLManager;
import com.ericlam.mc.votesystem.counter.VoteStats;
import com.ericlam.mc.votesystem.counter.VoteStatsManager;
import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

public class VotingListener implements Listener, ForwardedVoteListener {

    private VoteMySQLManager voteMySQLManager;
    private VoteStatsManager voteStatsManager;

    public VotingListener(){
        this.voteMySQLManager = VoteMySQLManager.getInstance();
        this.voteStatsManager = VoteStatsManager.getInstance();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVotingRecived(VotifierEvent e){
        Vote vote = e.getVote();
        VoterUtils.runAsync(()->this.onForward(vote));
    }

    @Override
    public void onForward(Vote vote) {
        ProxiedPlayer voter = ProxyServer.getInstance().getPlayer(vote.getUsername());
        if (voter == null) return;
        VoteStats stats = voteMySQLManager.getPlayerVote(voter.getUniqueId());
        long voteTime = vote.getLocalTimestamp();
        boolean inLobby = VoterUtils.inLobby(voter);

        if (!voteStatsManager.checkTimeStamp(voter.getUniqueId(), voteTime)){
            VoterUtils.log("Player "+voter.getName()+" has voted today, skipped the forward");
        }

        voteStatsManager.setTimeStamp(voter.getUniqueId(),voteTime);

        if (!voter.isConnected() || !inLobby){
            int queue = stats.getQueueVote();
            stats.setQueueVote(++queue);
            return;
        }

        VoterUtils.reward(voter, 1);

        voteStatsManager.editVotes(voter.getUniqueId(), 1,true);

    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e){
        VoterUtils.runAsync(()->voteMySQLManager.getPlayerVote(e.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(ServerDisconnectEvent e){
        VoterUtils.runAsync(()->voteMySQLManager.saveVote(e.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onServerSwitch(ServerConnectedEvent e){
        ProxiedPlayer player = e.getPlayer();
        if (!VoterUtils.isLobby(e.getServer().getInfo())) return;
        int queued = voteStatsManager.getQueuedVote(player.getUniqueId());
        if (queued < 1) return;
        VoterUtils.reward(player, queued);
        voteStatsManager.editVotes(player.getUniqueId(), queued, true);
        voteStatsManager.clearQueueVote(player.getUniqueId());
    }
}
