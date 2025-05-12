package com.example.soop.domain.knowledge;

import com.example.soop.global.knowledge.GeminiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiChatService geminiChatService;
    @Operation(summary = "Gemini와 채팅하기", description = "Gemini와 채팅하기")
    @PostMapping("/ask")
    public String ask(@RequestBody Map<String, Object> body) {
        Long chatRoomId = Long.parseLong(body.get("chatRoomId").toString());
        String question = (String) body.get("question");
        if (question == null || question.isBlank()) {
            return "❗질문 내용이 비어 있습니다.";
        }
        return geminiChatService.ask(question, chatRoomId);
    }

}
