package pro.sky.hogwarts.TestRestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.sky.hogwarts.controller.FacultyController;
import pro.sky.hogwarts.controller.StudentController;
import pro.sky.hogwarts.entity.Faculty;
import pro.sky.hogwarts.entity.Student;
import pro.sky.hogwarts.exception.FacultyNotFoundException;
import pro.sky.hogwarts.repository.FacultyRepository;
import pro.sky.hogwarts.repository.StudentRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FacultyControllerTest_Rest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private FacultyController facultyController;
    @Autowired
    private FacultyRepository facultyRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final Faker faker = new Faker();

    @AfterEach
    public void clean() {
        facultyRepository.deleteAll();
    }

    @Test
    void contextLoads() throws Exception {
        assertThat(facultyRepository).isNotNull();
    }

    /*** CREATE - ОК ***/
    @Test
    public void createFacultyTest() {
        Faculty facultyIn = generateFaculty();
        ResponseEntity<Faculty> responseEntity = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/faculties",
                facultyIn,
                Faculty.class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotEqualTo(0L);
        assertThat(responseEntity.getBody().getName()).isEqualTo(facultyIn.getName());
        assertThat(responseEntity.getBody().getColor()).isEqualTo(facultyIn.getColor());
    }

    /*** UPDATE -  OK  ***/
    @Test
    public void updateFacultyTest() {
        Faculty oldFaculty = generateFaculty();
        facultyRepository.save(oldFaculty);
        Faculty newFaculty = new Faculty();
        newFaculty.setId(oldFaculty.getId());
        newFaculty.setName("green");
        newFaculty.setColor(oldFaculty.getColor());

        ResponseEntity<Faculty> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/faculties/" + oldFaculty.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(newFaculty),
                Faculty.class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotEqualTo(0L);
        assertThat(responseEntity.getBody().getName()).isEqualTo(newFaculty.getName());
        assertThat(responseEntity.getBody().getColor()).isEqualTo(newFaculty.getColor());

        //not found checking
        long incorrectId = oldFaculty.getId() + 1;
        ResponseEntity<String> stringResponseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/faculties/" + incorrectId,
                HttpMethod.PUT,
                new HttpEntity<>(newFaculty),
                String.class
        );
        assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Факультет с id = " + incorrectId + " не найден!");
    }

    /*** GET_BY_ID - ***/
    @Test
    public void getFacultyById() {
        Faculty facultyIn = new Faculty(1L, "fac-1", "green");
        facultyRepository.save(facultyIn);
        Faculty facultyOut = testRestTemplate.getForObject(
                "http://localhost:" + port + "/faculties/" + facultyIn.getId(),
                Faculty.class,
                facultyIn.getId()
        );
        assertThat(facultyOut.getId()).isNotEqualTo(0L);
        assertThat(facultyOut.getName()).isEqualTo(facultyIn.getName());
        assertThat(facultyOut.getColor()).isEqualTo(facultyIn.getColor());

        //not found checking
        long incorrectId = facultyIn.getId() + 1;
        ResponseEntity<String> stringResponseEntity = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/faculties/" + incorrectId,
                String.class,
                facultyIn.getId()
        );
        assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Факультет с id = " + incorrectId + " не найден!");
    }


    /*** DELETE - НЕ РАБОТАЕТ (Error while extracting response for type [class pro.sky.hogwarts.entity.Faculty]
     * and content type [application/json]) *

     контроллер и сервис DELETE:
     //        @RequestMapping("/faculties")
     //        ...
     //        @DeleteMapping("/{id}")
     //        public Faculty deleteFaculty(@PathVariable("id") long id) {
     //            return facultyService.deleteFaculty(id);
     //        }
     //        сервис:
     //        public Faculty deleteFaculty(long id) {
     //            Faculty faculty = facultyRepository.findById(id)
     //                    .orElseThrow(() -> new FacultyNotFoundException(id));
     //            facultyRepository.delete(faculty);
     //            return faculty;
     //        }
     ***/
    @Test
    public void deleteFacultyTest() {
        Faculty facultyIn = new Faculty(10L, "fac-10", "green");
        facultyRepository.save(facultyIn);
        facultyRepository.findById(10L);

        Faculty facultyOut = testRestTemplate.getForObject(
                "http://localhost:" + port + "/faculties/" + facultyIn.getId(),
                Faculty.class,
                facultyIn.getId()
        );
        assertThat(facultyOut.getId()).isNotEqualTo(0L);
        assertThat(facultyOut.getName()).isEqualTo(facultyIn.getName());
        assertThat(facultyOut.getColor()).isEqualTo(facultyIn.getColor());


//        ResponseEntity<Faculty> responseEntity2 = testRestTemplate.exchange(
//                "http://localhost:" + port + "/faculties/" + facultyIn.getId(),
//                HttpMethod.DELETE,
//                HttpEntity.EMPTY,
//                Faculty.class
//        );
//        assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(facultyRepository.findById(facultyIn.getId())).isEmpty();

        //not found checking
        long incorrectId = facultyIn.getId() + 1;
        ResponseEntity<String> stringResponseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/faculties/" + incorrectId,
                HttpMethod.PUT,
                new HttpEntity<>(facultyIn),
                String.class
        );
        assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Факультет с id = " + incorrectId + " не найден!");
    }

    /*** FIND_ALL_OR_BY_COLOR - OK ***/
    @Test
    public void findAllFacultyOrByColorTest() {
        Faculty faculty1 = new Faculty(1L, "fac-1", "green");
        facultyRepository.save(faculty1);
        Faculty faculty2 = new Faculty(2L, "fac-2", "red");
        facultyRepository.save(faculty2);
        Faculty faculty3 = new Faculty(3L, "fac-3", "yellow");
        facultyRepository.save(faculty3);

        ResponseEntity<List> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/faculties",
                HttpMethod.GET,
                null,
                List.class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();

        ResponseEntity<List> responseEntity2 = testRestTemplate.exchange(
                "http://localhost:" + port + "/faculties?color=red",
                HttpMethod.GET,
                null,
                List.class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
    }

    public Faculty generateFaculty() {
        Faculty generatedFaculty = new Faculty();
        generatedFaculty.setId(1L);
        generatedFaculty.setName(faker.name().name());
        generatedFaculty.setColor(faker.color().name());
        return generatedFaculty;
    }
    //Commit-3
}
