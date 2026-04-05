package com.dormitory.management.repository;

import com.dormitory.management.entity.Issue;
import com.dormitory.management.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    @Query("""
            select distinct i
            from Issue i
            left join fetch i.student s
            left join fetch i.room r
            left join fetch r.building
            where s.id = :studentId
            order by i.createdAt desc
            """)
    List<Issue> findDetailedByStudentId(Long studentId);

    @Query("""
            select distinct i
            from Issue i
            left join fetch i.student s
            left join fetch i.room r
            left join fetch r.building
            order by i.createdAt desc
            """)
    List<Issue> findAllDetailed();
}
