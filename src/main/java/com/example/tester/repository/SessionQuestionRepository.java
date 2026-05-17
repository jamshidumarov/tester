package com.example.tester.repository;

import com.example.tester.entity.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, Long> {

    @Query("SELECT sq FROM SessionQuestion sq JOIN FETCH sq.question q LEFT JOIN FETCH q.options WHERE sq.session.id = :sessionId ORDER BY sq.displayOrder")
    List<SessionQuestion> findBySessionIdWithQuestionsAndOptions(@Param("sessionId") Long sessionId);
}
