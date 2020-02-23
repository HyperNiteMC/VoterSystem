package com.ericlam.mc.votesystem.bungee.mysql;

import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.votesystem.bungee.counter.VoteStats;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.HookType;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/*
    Using SQL Query Builders v3.0.0

 */
public class VoteTable {
    private DbTable voteStats;
    private DbColumn uuid;
    private DbColumn name;
    private DbColumn votes;
    private DbColumn timestamp;
    private DbColumn queued;

    public VoteTable(){
        this.voteStats = new DbSpec().addDefaultSchema().addTable("Vote_stats");
        this.uuid = this.voteStats.addColumn("PlayerUUID","VARCHAR",40);
        this.uuid.primaryKey();
        this.uuid.notNull();
        this.name = this.voteStats.addColumn("PlayerName","TINYTEXT",null);
        this.name.notNull();
        this.votes = this.voteStats.addColumn("Votes","INT",null);
        this.votes.notNull();
        this.timestamp = this.voteStats.addColumn("TimeStamp","BIGINT",null);
        this.timestamp.notNull();
        this.queued = this.voteStats.addColumn("Queued","INT",null);
        this.queued.notNull();
    }

    public String getCreateTableStatment(){
        return new CreateTableQuery(voteStats,true).addCustomization(CreateTableQuery.Hook.TABLE, HookType.AFTER,"IF NOT EXISTS ").validate().toString();
    }

    public String prepareInsert(){
        return new InsertQuery(voteStats).addPreparedColumns(this.uuid, this.name, this.votes, this.timestamp, this.queued).validate().toString() +
                " ON DUPLICATE KEY UPDATE " +
                this.name.getColumnNameSQL() + "=?," +
                this.votes.getColumnNameSQL() + "=?," +
                this.timestamp.getColumnNameSQL() + "=?," +
                this.queued.getColumnNameSQL() + "=?";
    }

    public boolean setPrepareStatment(PreparedStatement statement, UUID player, VoteStats voteStats) throws SQLException {
        String uuid = player.toString();
        try {
            System.out.println("statement uuid:" + player.toString());
            Optional<OfflinePlayer> offlinePlayer = HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(player).get();
            if (offlinePlayer.isEmpty()) {
                System.out.println("offlineplayer is empty");
                return false;
            }
            String name = offlinePlayer.get().getName();
            int votes = voteStats.getVotes();
            long timestamp = voteStats.getTimeStamp();
            int queued = voteStats.getQueueVote();
            statement.setString(1, uuid);
            statement.setString(2, name);
            statement.setInt(3, votes);
            statement.setLong(4, timestamp);
            statement.setInt(5, queued);
            statement.setString(6, name);
            statement.setInt(7, votes);
            statement.setLong(8, timestamp);
            statement.setInt(9, queued);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String selectStatment(UUID player){
        return new SelectQuery().addFromTable(voteStats).addAllColumns().addCondition(BinaryCondition.equalTo("PlayerUUID", player.toString())).validate().toString().replace("t0 ", "");
    }


}
