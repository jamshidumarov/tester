package com.example.tester.repository;

import com.example.tester.entity.SessionStatus;
import com.example.tester.entity.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    List<TestSession> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndStatusOrderByStartedAtDesc(
            String firstName, String lastName, SessionStatus status);
}
