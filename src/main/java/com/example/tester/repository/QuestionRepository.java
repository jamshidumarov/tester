package com.example.tester.repository;

import com.example.tester.entity.Question;
import com.example.tester.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM questions ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("count") int count);

    @Query(value = "SELECT * FROM questions WHERE subject_id = :subjectId ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Question> findRandomQuestionsBySubjectId(@Param("count") int count, @Param("subjectId") Long subjectId);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id IN :ids")
    List<Question> findAllWithOptionsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT q FROM Question q WHERE LOWER(q.text) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY q.id")
    List<Question> findByTextContaining(@Param("search") String search);

    @Query("SELECT q FROM Question q WHERE q.subject.id = :subjectId ORDER BY q.id")
    List<Question> findBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT q FROM Question q WHERE q.subject.id = :subjectId AND LOWER(q.text) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY q.id")
    List<Question> findBySubjectIdAndTextContaining(@Param("subjectId") Long subjectId, @Param("search") String search);

    List<Question> findBySubjectIsNull();

    long countBySubject(Subject subject);

    long countBySubjectId(Long subjectId);
}
