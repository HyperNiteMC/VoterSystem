package com.ericlam.mc.votesystem.bungee.counter;

import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.votesystem.bungee.VoterUtils;
import com.ericlam.mc.votesystem.bungee.main.VoterSystemBungee;
import com.ericlam.mc.votesystem.bungee.mysql.VoteTable;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class VoteMySQLManager {
    private VoteStatsManager voteStatsManager;
    private RedisCommitManager redisCommitManager;
    private VoteTable voteTable;

    public VoteMySQLManager() {
        this.voteTable = new VoteTable();
        this.voteStatsManager = VoterSystemBungee.INSTANCE.getVoteStatsManager();
        this.redisCommitManager = VoterSystemBungee.INSTANCE.getRedisCommitManager();
    }

    @Nonnull
    public VoteStats getPlayerVote(UUID playerUniqueId){
        if (voteStatsManager.containVote(playerUniqueId)) return voteStatsManager.getVote(playerUniqueId);
        String stmt = "SELECT * FROM `Vote_stats` WHERE `PlayerUUID`=?";
        try(Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection();
            PreparedStatement statement = connection.prepareStatement(stmt)){
            statement.setString(1, playerUniqueId.toString());
            ResultSet resultSet = statement.executeQuery();
            VoteStats stats;
            if (resultSet.next()){
                int vote = resultSet.getInt("Votes");
                long time = resultSet.getLong("TimeStamp");
                int queueVote = resultSet.getInt("Queued");
                stats = new VoteStats(vote, time, queueVote);
            }else{
                VoterUtils.debug("player don't have vote data, create one...");
                stats =  new VoteStats(0,0,0);
            }
            voteStatsManager.putVote(playerUniqueId, stats);
            redisCommitManager.commitStats(playerUniqueId, stats);
            return stats;
        } catch (SQLException e) {
            e.printStackTrace();
            return new VoteStats(0,0,0);
        }
    }

    public void saveVote(){
        Map<UUID, VoteStats> voteStatsMap = voteStatsManager.getStats();
        try(Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection()){
            for (UUID uuid : voteStatsMap.keySet()) {
                VoteStats stats = voteStatsMap.get(uuid);
                if (stats.isNotChanged()) continue;
                this.saveVote(connection, uuid, stats);
                stats.setChanged(false);
            }
            redisCommitManager.removeAll(voteStatsMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveVote(UUID playerUniqueId){
        VoteStats stats = voteStatsManager.getVote(playerUniqueId);
        try(Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection()){
            this.saveVote(connection, playerUniqueId, stats);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveVote(Connection connection, UUID playerUniqueId, VoteStats stats) throws SQLException {
        String insertPrepare = voteTable.prepareInsert();
        try(PreparedStatement statement = connection.prepareStatement(insertPrepare)){
            if (stats.isNotChanged()) {
                return;
            }
            if (voteTable.setPrepareStatment(statement, playerUniqueId, stats)) {
                statement.execute();
                VoterUtils.debug("Saved " + playerUniqueId.toString() + "'s data.");
            }
        }
    }

    public void createDatabase(){
        String createTable = voteTable.getCreateTableStatment();
        try(Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection();
        PreparedStatement statement = connection.prepareStatement(createTable)){
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRedis(){
        redisCommitManager.commitStats(voteStatsManager.getStats());
    }

}
