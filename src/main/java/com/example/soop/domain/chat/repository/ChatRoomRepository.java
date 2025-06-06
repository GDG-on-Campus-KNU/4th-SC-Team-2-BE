package com.example.soop.domain.chat.repository;

import com.example.soop.domain.chat.RoomType;
import com.example.soop.domain.chat.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 특정 유저와 대상 유저 간의 채팅방을 찾는 쿼리
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "JOIN cr.memberships m1 " +
           "JOIN cr.memberships m2 " +
           "WHERE m1.user.id = :userId AND m2.user.id = :targetUserId")
    Optional<ChatRoom> findByUserIdAndTargetUserId(
        @Param("userId") Long userId,
        @Param("targetUserId") Long targetUserId);

    /**
     * 특정 유저가 속한 채팅방들을 찾는 쿼리 - 인덱스 오름차순
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "JOIN cr.memberships m " +
           "WHERE m.user.id = :userId")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    /**
     * 특정 유저가 속한 채팅방들을 찾는 쿼리 - 가장 최근 메시지 도착 시간 내림차순
     */
    @Query("SELECT cr FROM ChatRoom cr " +
        "JOIN cr.memberships m " +
        "WHERE m.user.id = :userId " +
        "ORDER BY cr.messageUpdatedAt DESC")
    List<ChatRoom> findAllByUserIdOrderByMessageUpdatedAtDesc(@Param("userId") Long userId);

    /**
     * 룸 타입별 사용자 채팅방 목록 조회
     */
    @Query(
        "SELECT cr FROM ChatRoom cr " +
            "JOIN cr.memberships m " +
            "WHERE m.user.id = :userId " +
            "AND cr.roomType = :roomType " +
            "ORDER BY cr.messageUpdatedAt DESC"
    )
    List<ChatRoom> findAllByUserIdAndRoomTypeOrderByMessageUpdatedAtDesc(
        @Param("userId") Long userId,
        @Param("roomType") RoomType roomType
    );



}
