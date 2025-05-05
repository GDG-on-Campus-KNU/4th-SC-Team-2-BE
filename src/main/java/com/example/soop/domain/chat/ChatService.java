package com.example.soop.domain.chat;

import com.example.soop.domain.chat.dto.req.ChatRoomIdRequest;
import com.example.soop.domain.chat.dto.res.ChatContentResponse;
import com.example.soop.domain.chat.dto.res.ChatRoomResponse;
import com.example.soop.domain.chat.entity.Chat;
import com.example.soop.domain.chat.entity.ChatRoom;
import com.example.soop.domain.chat.entity.Membership;
import com.example.soop.domain.chat.entity.RoomStatus;
import com.example.soop.domain.chat.repository.ChatRepository;
import com.example.soop.domain.chat.repository.ChatRoomRepository;
import com.example.soop.domain.chat.repository.MemberShipRepository;
import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserRepository;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.UserException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberShipRepository memberShipRepository;
    private final UserRepository userRepository;

    /**
     * 채팅방 생성 또는 조회
     */
    @Transactional
    public ChatRoom createOrGetChatRoom(Long userId, ChatRoomIdRequest chatRoomIdRequest) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByUserIdAndTargetUserId(userId,
            chatRoomIdRequest.targetUserId());

        // 채팅방이 존재하는 경우
        if (chatRoom.isPresent()) {
            return chatRoom.get(); // 채팅방 반환
        }

        // 채팅방이 존재하지 않는 경우 -> 새로 생성
        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setTitle("Chat Room between " + userId + " and " + chatRoomIdRequest.targetUserId()); // 제목 설정
        newChatRoom.setStatus(RoomStatus.ENABLED); // 기본 상태 설정
        newChatRoom.setMessageUpdatedAt(LocalDateTime.now()); // 가장 최근 메시지 도착 시간을 현재 시간으로 설정
        // 여기서 targetUserId가 0이면 챗봇으로 판단
        if (chatRoomIdRequest.targetUserId() == 0) {
            newChatRoom.setRoomType(RoomType.USER_TO_BOT); // 챗봇과의 대화방
        } else {
            newChatRoom.setRoomType(RoomType.USER_TO_EXPERT); // 관리자와의 대화방 (또는 USER_TO_USER, 상황에 따라)
        }

        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

        if (!isUserInChatRoom(userId, savedChatRoom.getId())) {
            addUserToChatRoom(userId, savedChatRoom.getId());
        }
        if (chatRoomIdRequest.targetUserId() != 0 && !isUserInChatRoom(chatRoomIdRequest.targetUserId(), savedChatRoom.getId())) {
            addUserToChatRoom(chatRoomIdRequest.targetUserId(), savedChatRoom.getId());
        }

        return savedChatRoom;
    }

    /**
     * 채팅방의 채팅 목록 조회
     */
    public List<ChatContentResponse> getChatsByRoomId(Long chatRoomId) {
        List<Chat> chats = chatRepository.findAllByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
        List<ChatContentResponse> chatContentResponses = chats.stream().map(
            chat -> new ChatContentResponse(
                chat.getId(),
                chat.getChatRoomId(),
                chat.getSenderId(),
                chat.getContent(),
                chat.getCreatedAt()
            )
        ).toList();
        return chatContentResponses;
    }


    /**
     * 유저의 모든 채팅방 조회
     */
    public List<ChatRoomResponse> getChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserIdOrderByMessageUpdatedAtDesc(userId);

        List<ChatRoomResponse> chatRoomResponses = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            // MongoDB에서 최신 채팅 1개만 가져오기
            Optional<Chat> latestChatOptional = chatRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId());

            String latestChatContent = "대화 기록이 없습니다."; // 기본값
            if (latestChatOptional.isPresent()) {
                latestChatContent = latestChatOptional.get().getContent();
            }

            // RDB에서 나 제외한 멤버십 조회
            List<Membership> memberships = memberShipRepository.findMembershipsByChatRoomIdAndExcludeUser(
                chatRoom.getId(), userId
            );

            if (memberships.isEmpty()) {
                log.info("해당 채팅방의 참여 유저(membership) 이 존재하지 않습니다.");
                continue;
            }

            User targetUser = memberships.get(0).getUser();

            ChatRoomResponse response = new ChatRoomResponse(
                chatRoom.getId(),
                targetUser.getId(),
                targetUser.getEmail(),
                targetUser.getNickname(),
                chatRoom.getTitle(),
                latestChatContent,
                checkIsNew(chatRoom.getId(), userId),
                chatRoom.getStatus()
            );

            chatRoomResponses.add(response);
        }

        return chatRoomResponses;
    }



    /**
     * 채팅 저장
     */
    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }

    /**
     * 유저 채팅방 입장
     */
    public Membership addUserToChatRoom(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        return memberShipRepository.save(new Membership(user, chatRoom));
    }

    /**
     * 유저가 채팅방에 이미 존재하는지 확인
     */
    public boolean isUserInChatRoom(Long userId, Long chatRoomId) {
        return memberShipRepository.existsByUserIdAndChatRoomId(userId, chatRoomId);
    }

    /**
     * 채팅방 조회
     */
    public ChatRoom getChatRoom(Long id) {
        return chatRoomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    /**
     * 채팅방 최근 메시지 시간 업데이트
     */
    @Transactional
    public void updateChatRoomMessageUpdatedTime(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).get();
        chatRoom.updateMessageUpdatedAt();
    }

    /**
     * 특정 채팅방의 가장 최근 메시지가 상대방이 보낸 것이면서, 읽지 않은 경우 확인
     */
    public Boolean checkIsNew(Long chatRoomId, Long myId) {
        Optional<Chat> topChat = chatRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
        return topChat
            .filter(chat -> !chat.getSenderId().equals(myId)) // senderId(Long) 비교
            .filter(chat -> Boolean.FALSE.equals(chat.getIsRead()))
            .isPresent();
    }

    /**
     * 채팅방의 상대방이 보낸 메시지 모두 읽음 처리
     * userId - 나의 ID
     */
    @Transactional
    public void makeAllChatRead(Long chatRoomId, Long userId) {
        List<Membership> memberships = memberShipRepository.findMembershipsByChatRoomIdAndExcludeUser(
            chatRoomId, userId);
        // 해당 채팅방의 상대방 조회
        User appositeUser = memberships.get(0).getUser();
        // 상대방이 보낸 모든 메시지 조회
        List<Chat> chatsByAppositeUser = chatRepository.findAllByChatRoomIdAndSenderId(
            chatRoomId, appositeUser.getId());
        // 상대방이 보낸 모든 메시지 읽음 처리
        chatsByAppositeUser.stream().forEach(chat -> chat.setIsRead(true));
    }

    /**
     * (현재 채팅방일때 프론트엔드가 호출) 상대방이 보낸 특정 메시지 읽음 처리
     */
    @Transactional
    public void makeChatRead(Long chatRoomId, String chatId, Long myId) {
        Chat chat = chatRepository.findByIdAndChatRoomId(chatId, chatRoomId).get();
        // 내가 아니라, 상대방이 보낸 메시지인 경우 읽음 처리
        if(!chat.getSenderId().equals(myId)){
            chat.setIsRead(true);
        }
    }

    /**
     * 채팅방 룸 타입 반환 (USER_TO_USER / USER_TO_BOT / USER_TO_ADMIN)
     */
    public RoomType getRoomType(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).get();
        return chatRoom.getRoomType();
    }

    /**
     * 해당 채팅방의 최근 대화 내역 가져옴
     */
    public List<Chat> getRecentChats(Long chatRoomId, int limit) {
        // MongoDB는 .getContent()가 필요 없음
        return chatRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, PageRequest.of(0, limit));
    }
}
