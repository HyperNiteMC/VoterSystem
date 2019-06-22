package com.ericlam.mc.votesystem.redis;

import redis.clients.jedis.JedisPubSub;

public class ChannelListener extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        super.onMessage(channel, message);
    }
}
