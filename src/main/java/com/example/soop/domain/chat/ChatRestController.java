package com.example.soop.domain.chat;

import com.example.soop.domain.chat.dto.req.ChatRoomIdRequest;
import com.example.soop.domain.chat.dto.req.CreateAIChatRoomRequest;
import com.example.soop.domain.chat.dto.req.CreateChatRoomInfoRequest;
import com.example.soop.domain.chat.dto.res.AIChatRoomInfoResponse;
import com.example.soop.domain.chat.dto.res.AIChatRoomResponse;
import com.example.soop.domain.chat.dto.res.AIChatRoomsResponse;
import com.example.soop.domain.chat.dto.res.ChatContentResponse;
import com.example.soop.domain.chat.dto.res.ChatContentsResponse;
import com.example.soop.domain.chat.dto.res.ChatRoomIdResponse;
import com.example.soop.domain.chat.dto.res.ChatRoomResponse;
import com.example.soop.domain.chat.dto.res.ChatRoomsResponse;
import com.example.soop.domain.chat.entity.ChatRoom;
import com.example.soop.global.format.ApiResponse;
import com.example.soop.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    /**
     * [사용자 ↔ 챗봇]
     */
    @Operation(summary = "AI 챗봇 생성", description = "AI 챗봇 정의 정보를 생성합니다.")
    @PostMapping("/bots")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<String> createChatRoomInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody CreateChatRoomInfoRequest request
    ) {
        chatService.createChatRoomInfo(userDetails.getId(), request);
        return ApiResponse.createSuccess("AI 챗봇 정보가 생성되었습니다.");
    }

    /**
     * [사용자 ↔ 챗봇]
     */
    @Operation(summary = "사용자 AI 챗봇 리스트 조회", description = "사용자가 생성한 AI 챗봇 목록을 조회합니다.")
    @GetMapping("/bots")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<List<AIChatRoomInfoResponse>> getUserChatBots(@AuthenticationPrincipal CustomUserDetails userDetail) {
        List<AIChatRoomInfoResponse> chatBots = chatService.getUserChatBots(userDetail.getId());
        return ApiResponse.createSuccessWithData(chatBots);
    }

    /**
     * [사용자 ↔ 챗봇]
     */
    @Operation(summary = "AI 챗봇 채팅방 생성", description = "사용자가 정의한 AI 챗봇의 특징에 따라 채팅방을 생성합니다.")
    @PostMapping("/rooms/ai")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<AIChatRoomInfoResponse> createAIChatRoom(
        @Valid @RequestBody CreateAIChatRoomRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetail
    ) {
        AIChatRoomInfoResponse aiChatRoomInfoResponse = chatService.createAIChatRoom(userDetail.getId(),
            request);
        return ApiResponse.createSuccessWithData(aiChatRoomInfoResponse, "AI 챗봇 채팅방이 생성되었습니다.");
    }

    /**
     * [사용자 ↔ 챗봇]
     */
    @Operation(summary = "사용자-챗봇간 채팅방 목록 조회", description = "유저의 사용자-챗봇간 채팅방의 목록을 조회합니다.")
    @GetMapping("/rooms/ai")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<AIChatRoomsResponse> getAllAIChatRooms(
        @AuthenticationPrincipal CustomUserDetails userDetail
    ) {
        List<AIChatRoomResponse> aiChatRooms = chatService.getAIChatRooms(userDetail.getId());
        return ApiResponse.createSuccessWithData(new AIChatRoomsResponse(aiChatRooms));
    }

    /**
     * [사용자 ↔ 챗봇]
     */
    @Operation(summary = "사용자-챗봇간 특정 채팅방 조회", description = "유저의 사용자-챗봇간 특정 채팅방을 조회합니다.")
    @GetMapping("/rooms/ai/{roomId}")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<AIChatRoomResponse> getAIChatRoom(
        @AuthenticationPrincipal CustomUserDetails userDetail,
        @PathVariable("roomId") Long roomId
    ) {
        AIChatRoomResponse aiChatRoom = chatService.getAIChatRoom(roomId);
        return ApiResponse.createSuccessWithData(aiChatRoom);
    }


    /**
     * [사용자 ↔ 사용자]
     */
    @Operation(summary = "특정 상대와의 채팅방 조회/생성", description = "기존 채팅방이 있으면 해당 채팅방 ID를, 없으면 생성 후 ID 반환합니다.")
    @PostMapping("/rooms/users")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<ChatRoomIdResponse> getChatRoomId(
        @Valid @RequestBody ChatRoomIdRequest chatRoomIdRequest,
        @AuthenticationPrincipal CustomUserDetails userDetail
    ) {
        ChatRoom chatRoom = chatService.createOrGetChatRoom(userDetail.getId(), chatRoomIdRequest);
        return ApiResponse.createSuccessWithData(new ChatRoomIdResponse(chatRoom.getId()));
    }

    /**
     * [사용자 ↔ 사용자]
     */
    @Operation(summary = "사용자간 채팅방 목록 조회", description = "유저의 사용자간 채팅방의 목록을 조회합니다.")
    @GetMapping("/rooms/users")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<ChatRoomsResponse> getAllUserChatRooms(
        @AuthenticationPrincipal CustomUserDetails userDetail
    ) {
        List<ChatRoomResponse> userChatRooms = chatService.getUserChatRooms(userDetail.getId());
        return ApiResponse.createSuccessWithData(new ChatRoomsResponse(userChatRooms));
    }

    /**
     * [공통]
     */
    @Operation(summary = "채팅방 메시지 목록 조회", description = "특정 채팅방의 메시지 목록을 조회합니다.")
    @GetMapping("/{chatRoomId}/messages")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<ChatContentsResponse> getMessages(@PathVariable Long chatRoomId) {
        List<ChatContentResponse> chatContentResponses = chatService.getChatsByRoomId(chatRoomId);
        return ApiResponse.createSuccessWithData(new ChatContentsResponse(chatContentResponses));
    }


    /**
     * [공통]
     */
    @Operation(summary = "채팅방의 상대방 보낸 메시지 모두 읽음 처리", description = "상대방이 보낸 메시지를 모두 읽음 처리합니다.")
    @PostMapping("/{chatRoomId}/all/read")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<String> readAllChat(
        @AuthenticationPrincipal CustomUserDetails userDetail,
        @PathVariable("chatRoomId") Long chatRoomId
    ){
        chatService.makeAllChatRead(chatRoomId, userDetail.getId());
        return ApiResponse.createSuccess("상대방 메시지 모두 읽음 처리 완료");
    }

    /**
     * [공통]
     */
    @Operation(summary = "채팅방의 상대방 보낸 특정 메시지 읽음 처리", description = "상대방이 보낸 특정 메시지를 읽음 처리합니다.")
    @PostMapping("/{chatRoomId}/{chatId}/read")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<String> readChat(
        @AuthenticationPrincipal CustomUserDetails userDetail,
        @PathVariable("chatRoomId") Long chatRoomId,
        @PathVariable("chatId") String chatId
    ){
        chatService.makeChatRead(chatRoomId, chatId, userDetail.getId());
        return ApiResponse.createSuccess("id가 " + chatId + "인 메시지 읽음 처리 완료");
    }
}
