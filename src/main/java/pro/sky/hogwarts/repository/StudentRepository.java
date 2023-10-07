package pro.sky.hogwarts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.hogwarts.entity.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllStudentsByAge(int age);
    List<Student> findStudentsByAgeBetween(int ageFrom, int ageTo);
}
