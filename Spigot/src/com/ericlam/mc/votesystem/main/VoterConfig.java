package com.ericlam.mc.votesystem.main;

import com.hypernite.mc.hnmc.core.config.Prop;
import com.hypernite.mc.hnmc.core.config.yaml.MessageConfiguration;
import com.hypernite.mc.hnmc.core.config.yaml.Resource;

import java.util.Map;

@Resource(locate = "server.yml")
public class VoterConfig extends MessageConfiguration {

    @Prop(path = "server-name")
    private String server;

    @Prop
    private boolean debug;

    @Prop(path = "voted-today")
    private Map<String, String> votedToday;

    public String getServer() {
        return server;
    }

    public boolean isDebug() {
        return debug;
    }

    public Map<String, String> getVotedToday() {
        return votedToday;
    }
}
