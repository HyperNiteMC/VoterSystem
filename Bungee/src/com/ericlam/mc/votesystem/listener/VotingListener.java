package com.ericlam.mc.votesystem.listener;

import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.votesystem.VoterUtils;
import com.ericlam.mc.votesystem.counter.VoteMySQLManager;
import com.ericlam.mc.votesystem.counter.VoteStats;
import com.ericlam.mc.votesystem.counter.VoteStatsManager;
import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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
        this.onForward(vote);
    }

    @Override
    public void onForward(Vote vote) {
        HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(vote.getUsername()).thenApplyAsync((d) -> {
            if (d.isEmpty()) {
                VoterUtils.debug("cannot find " + vote.getUsername() + " in offline data, skipped");
                return null;
            }
            OfflinePlayer player = d.get();
            return new Wrapper(voteMySQLManager.getPlayerVote(player.getUniqueId()), player);
        }).whenComplete((wrapper, e) -> {
            if (wrapper == null) return;
            if (e == null) {
                OfflinePlayer voter = wrapper.player;
                VoteStats stats = wrapper.voteStats;
                long voteTime = vote.getLocalTimestamp();

                if (!voteStatsManager.checkTimeStamp(voter.getUniqueId(), voteTime)) {
                    VoterUtils.log("Player " + voter.getName() + " has voted today, skipped the forward");
                    return;
                }

                voteStatsManager.setTimeStamp(voter.getUniqueId(), voteTime);

                if (!voter.isOnline() || !VoterUtils.inLobby(voter.getPlayer())) {
                    int queue = stats.getQueueVote();
                    stats.setQueueVote(++queue);
                    VoterUtils.debug(voter.getName() + " is not in lobby server or is not online, added to queue votes...");
                    return;
                }

                VoterUtils.reward(voter.getPlayer(), 1);

                VoterUtils.debug("added one vote to " + voter.getName());
                voteStatsManager.editVotes(voter.getUniqueId(), 1, true);
            } else {
                e.printStackTrace();
            }
        });

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
        HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(e.getPlayer().getName()).whenCompleteAsync((p, ex) -> {
            if (p.isEmpty()) {
                VoterUtils.debug("cannot get offline data of " + e.getPlayer().getName());
                return;
            }
            OfflinePlayer player = p.get();
            if (ex == null) {
                if (!VoterUtils.isLobby(e.getServer().getInfo())) return;
                int queued = voteStatsManager.getQueuedVote(player.getUniqueId());
                if (queued < 1) return;
                VoterUtils.reward(player.getPlayer(), queued);
                voteStatsManager.editVotes(player.getUniqueId(), queued, true);
                voteStatsManager.clearQueueVote(player.getUniqueId());
            } else {
                ex.printStackTrace();
            }
        });

    }
}
