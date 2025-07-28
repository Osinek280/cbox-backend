package com.cbox.cbox.repositories;

import com.cbox.cbox.entities.user.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}