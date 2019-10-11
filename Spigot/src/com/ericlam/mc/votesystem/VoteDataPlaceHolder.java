package com.ericlam.mc.votesystem;

import com.ericlam.mc.votesystem.main.VoterConfig;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class VoteDataPlaceHolder extends PlaceholderExpansion {

    private final String YES;
    private final String NO;
    private Plugin plugin;
    private VoteDataManager voteDataManager;

    public VoteDataPlaceHolder(Plugin plugin, VoterConfig voterConfig) {
        this.plugin = plugin;
        this.voteDataManager = VoteDataManager.getInstance();
        this.YES = voterConfig.getVotedToday().get("true");
        this.NO = voterConfig.getVotedToday().get("false");
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        UUID uuid = p.getUniqueId();
        if (!voteDataManager.launched) {
            return "(資料獲取失敗, 稍後再嘗試)";
        }
        VoteData data = voteDataManager.getVoteData(uuid);
        switch (params.toLowerCase()) {
            case "votes":
                return data.getVotes() + "";
            case "voted-today":
                return data.isVotedToday() ? YES : NO;
            default:
                break;
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
