package com.example.tester.repository;

import com.example.tester.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    List<UserAnswer> findBySessionId(Long sessionId);

    Optional<UserAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);
}
