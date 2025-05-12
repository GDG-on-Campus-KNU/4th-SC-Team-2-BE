package com.example.soop.global.redis;

import com.example.soop.domain.chat.dto.res.ChatContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    public void publish(ChatContentResponse message) {
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }
}
