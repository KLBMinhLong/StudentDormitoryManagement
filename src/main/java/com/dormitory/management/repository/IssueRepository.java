package com.dormitory.management.repository;

import com.dormitory.management.entity.Issue;
import com.dormitory.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByStudent(Student student);
    List<Issue> findByStudent_Id(Long studentId);
}
