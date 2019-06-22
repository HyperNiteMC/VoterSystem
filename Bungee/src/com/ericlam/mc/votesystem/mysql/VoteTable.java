package com.ericlam.mc.votesystem.mysql;

import com.ericlam.mc.votesystem.counter.VoteStats;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.HookType;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import net.md_5.bungee.api.ProxyServer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
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

    public void setPrepareStatment(PreparedStatement statement, UUID player, VoteStats voteStats) throws SQLException {
        String uuid = player.toString();
        String name = ProxyServer.getInstance().getPlayer(player).getName();
        int votes = voteStats.getVotes();
        long timestamp = voteStats.getTimeStamp();
        int queued = voteStats.getQueueVote();
        statement.setString(1,uuid);
        statement.setString(2,name);
        statement.setInt(3,votes);
        statement.setLong(4,timestamp);
        statement.setInt(5,queued);
        statement.setString(6,name);
        statement.setInt(7,votes);
        statement.setLong(8,timestamp);
        statement.setInt(9,queued);

    }

    public String selectStatment(UUID player){
        return new SelectQuery().addFromTable(voteStats).addAllColumns().addCondition(BinaryCondition.equalTo(this.uuid.getColumnNameSQL(),player.toString())).validate().toString();
    }


}
