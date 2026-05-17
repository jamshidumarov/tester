package com.example.tester.repository;

import com.example.tester.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findAllByIdIn(List<Long> ids);
}
