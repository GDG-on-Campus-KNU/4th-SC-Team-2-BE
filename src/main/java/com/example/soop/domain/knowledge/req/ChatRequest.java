package com.example.soop.domain.knowledge.req;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    private Long chatRoomId;
    private String question;
    private List<Map<String, String>> history; // {"role": "user/assistant", "content": "..."}
}
