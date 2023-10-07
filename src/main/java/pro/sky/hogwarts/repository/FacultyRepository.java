package pro.sky.hogwarts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.hogwarts.entity.Faculty;

import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findAllByColor(String color);
    List<Faculty> findByColorContainingIgnoreCaseOrNameContainingIgnoreCase(String color, String name);
    //Commit-3
}
