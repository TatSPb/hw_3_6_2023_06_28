package pro.sky.hogwarts.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.hogwarts.entity.*;
import pro.sky.hogwarts.service.*;

import java.util.List;

@RestController
@Tag(name = "Контроллер по работе со студентами")
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentService.createStudent(student);
    }

    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable("id") long id) {
        return studentService.getStudentById(id);
    }

    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable("id") long id, @RequestBody Student student) {
        return studentService.updateStudent(id, student);
    }

    @DeleteMapping("/{id}")
    public Student deleteStudent(@PathVariable("id") long id) {
        return studentService.deleteStudent(id);
    }

    @GetMapping
    public List<Student> findAllStudentsByAge(@RequestParam(required = false) Integer age) {
        return studentService.findAllStudentsByAge(age);
    }

    @GetMapping("/filter")
    public List<Student> findStudentsByAgeBetween(@RequestParam int ageFrom, @RequestParam int ageTo) {
        return studentService.findStudentsByAgeBetween(ageFrom, ageTo);
    }

    @PatchMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Student uploadAvatar(@PathVariable("id") long id, @RequestPart("avatar") MultipartFile multipartFile) {
        return studentService.uploadAvatarToStudent(id, multipartFile);
    }
    //Commit-3
}
