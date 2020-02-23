package com.ericlam.mc.votesystem.bungee.commands;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNode;
import com.ericlam.mc.votesystem.bungee.VoterUtils;
import com.ericlam.mc.votesystem.bungee.main.VoterSystemBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class VoteCommand extends CommandNode {
    public VoteCommand() {
        super(null, "vote", null, "獲取投票網址", null, "v");
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> list) {
        if (!(commandSender instanceof ProxiedPlayer)){
            MessageBuilder.sendMessage(commandSender, ChatColor.RED + "You are not Player! try to use /votesys");
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (VoterUtils.notInLobby(player)) {
            MessageBuilder.sendMessage(player, VoterSystemBungee.voterConfig.getMessage("not-in-lobby"));
            return;
        }
        String[] send = VoterSystemBungee.voterConfig.getList("voteMessages").toArray(String[]::new);
        new MessageBuilder(send).sendPlayer(player);
    }

    @Override
    public List<String> executeTabCompletion(CommandSender commandSender, List<String> list) {
        return null;
    }
}
