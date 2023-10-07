package pro.sky.hogwarts.service;

import io.micrometer.common.lang.Nullable;
import org.springframework.stereotype.Service;

import pro.sky.hogwarts.entity.*;
import pro.sky.hogwarts.exception.*;
import pro.sky.hogwarts.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FacultyService {
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    public FacultyService(FacultyRepository facultyRepository, StudentRepository studentRepository) {
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        Faculty newFaculty = new Faculty();
        newFaculty.setId(faculty.getId());
        newFaculty.setName(faculty.getName());
        newFaculty.setColor(faculty.getColor());
        return newFaculty;
    }

    public Faculty getFacultyById(long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException(id));
    }

    public Faculty updateFaculty(long id, Faculty faculty) {
        return facultyRepository.findById(id)
                .map(oldFaculty -> {
                    oldFaculty.setColor(faculty.getColor());
                    oldFaculty.setName(faculty.getName());
                    return facultyRepository.save(oldFaculty);
                })
                .orElseThrow(() -> new FacultyNotFoundException(id));
    }

    public Faculty deleteFaculty(long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException(id));
        facultyRepository.delete(faculty);
        return faculty;
    }

    public List<Faculty> findFacultyByColor(@Nullable String color) {
        return new ArrayList<>(Optional.ofNullable(color)
                .map(facultyRepository::findAllByColor)
                .orElseGet(facultyRepository::findAll));
    }

    public List<Faculty> findFacultyByColorOrName(String colorOrName) {
        return new ArrayList<>(
                facultyRepository.findByColorContainingIgnoreCaseOrNameContainingIgnoreCase(colorOrName, colorOrName));
    }
    //Commit-3

}
