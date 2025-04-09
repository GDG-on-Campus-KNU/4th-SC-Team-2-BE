package com.example.soop.domain.emotion;

import com.example.soop.domain.emotion.req.CreateEmotionLogRequest;
import com.example.soop.domain.emotion.req.UpdateEmotionLogRequest;
import com.example.soop.domain.emotion.res.EmotionLogResponse;
import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserRepository;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.EmotionLogException;
import com.example.soop.global.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmotionLogService {


    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public EmotionLog createEmotionLog(Long userId, CreateEmotionLogRequest request) {
        User user = findUser(userId);
        EmotionLog emotionLog = new EmotionLog(user, request.emotionName(), request.emotionGroup(),
            request.content(), request.recordedAt());
        return emotionLogRepository.save(emotionLog);
    }

    @Transactional
    public void deleteEmotionLog(Long emotionLogId) {
        EmotionLog emotionLog = emotionLogRepository.findById(emotionLogId)
            .orElseThrow(() -> new EmotionLogException(ErrorCode.EMOTION_NOT_FOUND));
        emotionLogRepository.delete(emotionLog);
    }

    @Transactional
    public void updateEmotionLog(Long emotionLogId,
        UpdateEmotionLogRequest updateEmotionLogRequest) {
        EmotionLog emotionLog = emotionLogRepository.findById(emotionLogId)
            .orElseThrow(() -> new EmotionLogException(ErrorCode.EMOTION_NOT_FOUND));
        emotionLog.update(updateEmotionLogRequest.emotionName(),
            updateEmotionLogRequest.emotionGroup(), updateEmotionLogRequest.content(),
            updateEmotionLogRequest.recordedAt());
    }

    public EmotionLogResponse getEmotionLog(Long emotionLogId) {
        EmotionLog emotionLog = emotionLogRepository.findById(emotionLogId)
            .orElseThrow(() -> new EmotionLogException(ErrorCode.EMOTION_NOT_FOUND));
        return EmotionLogResponse.fromEntity(emotionLog);
    }

    public User findUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        return user;
    }
}
