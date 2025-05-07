package com.example.soop.domain.chat.repository;

import com.example.soop.domain.chat.entity.ChatRoomInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomInfoRepository extends JpaRepository<ChatRoomInfo, Long> {

    List<ChatRoomInfo> findAllByUserId(Long userId);
}