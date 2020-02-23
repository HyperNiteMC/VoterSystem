package com.ericlam.mc.votesystem.bungee;

import com.ericlam.mc.bungee.hnmc.config.yaml.MessageConfiguration;
import com.ericlam.mc.bungee.hnmc.config.yaml.Resource;

import java.util.List;

@Resource(locate = "config.yml")
public class VoterConfig extends MessageConfiguration {

    public Reward reward;
    public Announcement announcement;
    public List<String> voteMessages;
    public boolean debug;
    public List<String> whitelistServers;

    public String getMessage(String path) {
        return this.get("messages." + path);
    }

    public static class Reward {
        public List<String> spigotCommands;
        public boolean broadcast;
    }

    public static class Announcement {
        public boolean onJoin;
        public int interval;
    }

}
