package com.example.tester.repository;

import com.example.tester.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM questions ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("count") int count);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id IN :ids")
    List<Question> findAllWithOptionsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT q FROM Question q WHERE LOWER(q.text) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY q.id")
    List<Question> findByTextContaining(@Param("search") String search);
}
