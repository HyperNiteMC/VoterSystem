package com.ericlam.mc.votesystem.spigot.main;

import com.hypernite.mc.hnmc.core.config.yaml.Configuration;
import com.hypernite.mc.hnmc.core.config.yaml.Resource;

import java.util.Map;

@Resource(locate = "server.yml")
public class VoterConfig extends Configuration {

    private String serverName;
    private boolean debug;
    private Map<String, String> votedToday;

    public String getServer() {
        return serverName;
    }

    public boolean isDebug() {
        return debug;
    }

    public Map<String, String> getVotedToday() {
        return votedToday;
    }
}
