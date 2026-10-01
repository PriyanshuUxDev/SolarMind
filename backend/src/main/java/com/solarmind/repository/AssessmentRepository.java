package com.solarmind.repository;

import com.solarmind.entity.Assessment;
import com.solarmind.entity.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
  Optional<Assessment> findByIdAndUser(Long id, User user);

  List<Assessment> findTop10ByUserOrderByCreatedAtDesc(User user);

  Optional<Assessment> findFirstByUserOrderByCreatedAtDesc(User user);
}
