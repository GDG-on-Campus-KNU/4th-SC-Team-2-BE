package com.example.soop.domain.chat;

import com.example.soop.domain.chat.res.ChatRoomResponse;
import com.example.soop.global.format.ApiResponse;
import com.example.soop.global.security.CustomUserDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

    @PostMapping
    public ApiResponse<ChatRoomResponse> getOrCreateRoom(
        @RequestParam String botId,
        @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
        String userId = userDetails.getId().toString();
        String first = userId.compareTo(botId) < 0 ? userId : botId;
        String second = userId.compareTo(botId) < 0 ? botId : userId;
        // 항상 작은 id 가 앞에 오도록 저장 및 검색
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByBotIdAndUserId(first, second);
        if (chatRoom.isPresent()) {
            return ApiResponse.createSuccessWithData(ChatRoomResponse.fromEntity(chatRoom.get()),
                "기존 채팅방 정보가 반환되었습니다");
        }
        ChatRoom savedChatRoom = chatRoomRepository.save(new ChatRoom(first ,second));
        return ApiResponse.createSuccessWithData(ChatRoomResponse.fromEntity(savedChatRoom),
            "새로운 채팅방이 생성되어 해당 정보가 반환되었습니다.");
    }
}
