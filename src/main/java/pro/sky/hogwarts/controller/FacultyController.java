package pro.sky.hogwarts.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import pro.sky.hogwarts.entity.*;
import pro.sky.hogwarts.service.*;

import java.util.List;

@RestController
@Tag(name = "Контроллер по работе с факультетами")
@RequestMapping("/faculties")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping
    public Faculty createFaculty(@RequestBody Faculty faculty) {
        return facultyService.createFaculty(faculty);
    }

    @PutMapping("/{id}")
    public Faculty updateFaculty(@PathVariable("id") long id, @RequestBody Faculty faculty) {
        return facultyService.updateFaculty(id, faculty);
    }

    @GetMapping("/{id}")
    public Faculty getFacultyById(@PathVariable("id") long id) {
        return facultyService.getFacultyById(id);
    }

    @DeleteMapping("/{id}")
    public Faculty deleteFaculty(@PathVariable("id") long id) {
        return facultyService.deleteFaculty(id);
    }

    @GetMapping
    public List<Faculty> findAllFacultyOrByColor(@RequestParam(required = false) String color) {
        return facultyService.findFacultyByColor(color);
    }
    @GetMapping("/filter")
    public List<Faculty> findFacultyByColorOrName(@RequestParam String colorOrName) {
        return facultyService.findFacultyByColorOrName(colorOrName);
    }


}
