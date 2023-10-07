package pro.sky.hogwarts.service;

import io.micrometer.common.lang.Nullable;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.hogwarts.entity.Avatar;
import pro.sky.hogwarts.entity.Faculty;
import pro.sky.hogwarts.entity.Student;
import pro.sky.hogwarts.exception.FacultyNotFoundException;
import pro.sky.hogwarts.exception.StudentNotFoundException;

import pro.sky.hogwarts.repository.FacultyRepository;
import pro.sky.hogwarts.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AvatarService avatarService;

    public StudentService(StudentRepository studentRepository,
                          FacultyRepository facultyRepository,
                          AvatarService avatarService) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.avatarService = avatarService;
    }

    public Student createStudent(Student student) {
        Student newStudent = new Student();
        newStudent.setId(student.getId());
        newStudent.setName(student.getName());
        newStudent.setAge(student.getAge());
        return newStudent;
    }

    public Student getStudentById(long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

    }

    public Student updateStudent(long id, Student student) {
        return studentRepository.findById(id)
                .map(oldStudent -> {
                    oldStudent.setName(student.getName());
                    oldStudent.setAge(student.getAge());
                    Optional.ofNullable(student.getId());
                    Optional.ofNullable(student.getFaculty());
//                    .ifPresent(facultyId ->
//                                    oldStudent.setFaculty(
//                                            facultyRepository.findById(facultyId)
//                                                    .orElseThrow(() -> new FacultyNotFoundException(facultyId))
//                                    )
//                            );
                    return studentRepository.save(oldStudent);
                })
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    public Student deleteStudent(long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
        studentRepository.delete(student);
        return student;
    }

    public List<Student> findAllStudentsByAge(@Nullable Integer age) {
        return new ArrayList<>(Optional.ofNullable(age)
                .map(studentRepository::findAllStudentsByAge)
                .orElseGet(studentRepository::findAll));
    }

    public List<Student> findStudentsByAgeBetween(int ageFrom, int ageTo) {
        return studentRepository.findStudentsByAgeBetween(ageFrom, ageTo);
    }

    public Faculty findStudentFaculty(long id) {
        return studentRepository.findById(id)
                .map(Student::getFaculty)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    public Student uploadAvatarToStudent(long id, MultipartFile multipartFile) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
        Avatar avatar = avatarService.create(student, multipartFile);
        student.setAvatarUrl("http://localhost:8088/avatars/" + avatar.getId() + "/from-db");
        return student;
    }
}
