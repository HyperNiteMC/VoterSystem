package com.ericlam.mc.votesystem.commands;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNode;
import com.ericlam.mc.votesystem.VoterUtils;
import com.ericlam.mc.votesystem.main.VoterSystemBungee;
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
        if (!VoterUtils.inLobby(player)){
            MessageBuilder.sendMessage(player, VoterSystemBungee.getConfigManager().getMessage("messages.not-in-lobby"));
            return;
        }
        String[] send = VoterSystemBungee.getConfigManager().getMessageList("vote-messages",true);
        new MessageBuilder(send).sendPlayer(player);
    }

    @Override
    public List<String> executeTabCompletion(CommandSender commandSender, List<String> list) {
        return null;
    }
}
